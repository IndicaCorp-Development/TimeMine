package net.indicacorp.timemine.models;

import net.indicacorp.timemine.TimeMine;
import net.indicacorp.timemine.exceptions.InvalidWorldException;
import net.indicacorp.timemine.util.DatabaseHelper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class TimeMineBlock {
    public final long id;
    public final int x;
    public final int y;
    public final int z;
    public final World world;
    private boolean isMined;
    private Material displayBlock;
    private Material originalBlock;
    private Material dropItem;
    private String dropItemCount;
    private Date minedAt;
    private short resetInterval;
    private static TimeMine plugin;

    public TimeMineBlock(ResultSet e) throws SQLException, InvalidWorldException {
        plugin = TimeMine.getInstance();

        id = e.getLong("id");
        world = Bukkit.getServer().getWorld(e.getString("world"));
        if (world == null) throw new InvalidWorldException(this.id);
        x = e.getInt("x");
        y = e.getInt("y");
        z = e.getInt("z");

        isMined = e.getBoolean("isMined");
        displayBlock = Material.getMaterial(e.getString("displayBlock"));
        originalBlock = Material.getMaterial(e.getString("originalBlock"));
        dropItem = Material.getMaterial(e.getString("dropItem"));
        minedAt = e.getTimestamp("minedAt");
        resetInterval = e.getShort("resetInterval");
        dropItemCount = e.getInt("dropItemCount") > 64 ? "64" : e.getString("dropItemCount");
        if (e.getInt("dropItemRange") > 0 && e.getInt("dropItemRange") < 65) dropItemCount += "-" + e.getString("dropItemRange");

        if (displayBlock == null) displayBlock = Material.SMOOTH_STONE;
        if (originalBlock == null) originalBlock = Material.SMOOTH_STONE;
        if (dropItem == null) dropItem = Material.STICK;
    }

    public boolean isMined() {
        return isMined;
    }
    public void setMined(boolean mined) {
        isMined = mined;
        performUpdateQuery("UPDATE timemine SET isMined = " + mined + " WHERE id = " + this.id);
    }

    public Material getDisplayBlock() {
        return displayBlock;
    }
    public void setDisplayBlock(Material displayBlock) {
        this.displayBlock = displayBlock;
        performUpdateQuery("UPDATE timemine SET displayBlock = '" + displayBlock.toString() + "' WHERE id = " + this.id);
    }

    public Material getOriginalBlock() {
        return originalBlock;
    }
    public void setOriginalBlock(Material originalBlock) {
        this.originalBlock = originalBlock;
        performUpdateQuery("UPDATE timemine SET originalBlock = '" + originalBlock.toString() + "' WHERE id = " + this.id);
    }

    public Material getDropItem() {
        return dropItem;
    }
    public void setDropItem(Material dropItem) {
        this.dropItem = dropItem;
        performUpdateQuery("UPDATE timemine SET dropItem = '" + dropItem.toString() + "' WHERE id = " + this.id);
    }

    public String getDropItemCount() {
        return dropItemCount;
    }
    public void setDropItemCount(int dropItemCount, int dropItemRange) {
        this.dropItemCount = String.valueOf(dropItemCount);
        String sql = "UPDATE timemine SET dropItemCount = " + dropItemCount;
        if (dropItemRange > 0) {
            this.dropItemCount += "-" + dropItemRange;
            sql += ", dropItemRange = " + dropItemRange;
        }
        sql += " WHERE id = " + this.id;
        performUpdateQuery(sql);
    }

    public Date getMinedAt() {
        return minedAt;
    }
    public void updateMinedAt() {
        this.minedAt = new Date();
        performUpdateQuery("UPDATE timemine SET minedAt = CURRENT_TIMESTAMP WHERE id = " + this.id);
    }
    public void clearMinedAt() {
        this.minedAt = null;
        performUpdateQuery("UPDATE timemine SET minedAt = NULL WHERE id = " + this.id);
    }

    public short getResetInterval() {
        return resetInterval;
    }
    public void setResetInterval(short resetInterval) {
        this.resetInterval = resetInterval;
        performUpdateQuery("UPDATE timemine SET resetInterval = " + resetInterval + " WHERE id = " + this.id);
    }

    public void setBlockType(Material m) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            world.getBlockAt(x, y, z).setType(m);
        });
    }

    public String getComboId() {
        return this.x + "-" + this.y + "-" + this.z + "-" + this.world.getName();
    }

    private static void performUpdateQuery(String sql) {
        DatabaseHelper.insertOrUpdateAsync(sql);
    }
}
