package net.indicacorp.timemine.util;

import net.indicacorp.timemine.exceptions.BlockNotFoundException;
import net.indicacorp.timemine.exceptions.InvalidWorldException;
import net.indicacorp.timemine.models.TimeMineBlock;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class BlockCache {
    public static HashMap<String, TimeMineBlock> cache = new HashMap<>();

    public static HashMap<String, TimeMineBlock> getCache() {
        return cache;
    }

    public static TimeMineBlock getBlock(String comboId) {
        return cache.get(comboId);
    }

    public static TimeMineBlock addBlock(Block block, Material displayBlock, Material dropItem, short dropItemCount, short resetInterval) throws SQLException, InvalidWorldException {
        String sql = "INSERT INTO timemine (x, y, z, world, displayBlock, originalBlock, dropItem, dropItemCount, resetInterval) " +
                "VALUES (" +
                    block.getX() + ", " +
                    block.getY() + ", " +
                    block.getZ() + ", '" +
                    block.getWorld().getName() + "', '" +
                    displayBlock.toString() + "', '" +
                    block.getType().toString() + "', '" +
                    dropItem.toString() + "', " +
                    dropItemCount + ", " +
                    resetInterval +
                ")";
        long id = DatabaseHelper.insertOrUpdateSync(sql);
        if (id < 1) throw new SQLException("Something prevented block insertion to database");
        sql = "SELECT * FROM timemine WHERE id = " + id;
        ResultSet results = DatabaseHelper.query(sql);
        if (results != null) {
            if (results.next()) {
                TimeMineBlock t = new TimeMineBlock(results);
                cache.put(t.getComboId(), t);
                results.close();
                return t;
            }
        }
        throw new SQLException("WTF happened here? Somehow the returned generated key isn't saved to the database. You should probably submit an issue.");
    }

    public static void removeBlock(String comboId) throws BlockNotFoundException {
        TimeMineBlock b = cache.get(comboId);
        if (b == null) throw new BlockNotFoundException("Block with combo ID " + comboId + " does not exist");
        String sql = "DELETE FROM timemine WHERE id = " + b.id;
        DatabaseHelper.insertOrUpdateAsync(sql);
        b.setBlockType(b.getOriginalBlock());
        cache.remove(comboId);
    }

    public static void removeAllBlocks() {
        String sql = "TRUNCATE timemine";
        DatabaseHelper.insertOrUpdateAsync(sql);
        for (Map.Entry<String, TimeMineBlock> entry : cache.entrySet()) {
            TimeMineBlock b = entry.getValue();
            b.setBlockType(b.getOriginalBlock());
        }
        cache.clear();
    }

    public static void resetAllBlocks() {
        for (Map.Entry<String, TimeMineBlock> entry : cache.entrySet()) {
            TimeMineBlock b = entry.getValue();
            b.setMined(false);
            b.clearMinedAt();
            b.setBlockType(b.getDisplayBlock());
        }
    }

    public static void cacheBlocks() {
        try {
            String sql = "SELECT * FROM timemine";
            final ResultSet results = DatabaseHelper.query(sql);
            if (results != null) {
                while (results.next()) {
                    TimeMineBlock b = new TimeMineBlock(results);
                    cache.put(b.getComboId(), b);
                }
                results.close();
            }
        } catch(SQLException | InvalidWorldException e) {
            e.printStackTrace();
        }
    }
}
