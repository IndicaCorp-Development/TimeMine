package net.indicacorp.timemine.listeners;

import net.indicacorp.timemine.util.Database;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;

public class EntityExplodeListener implements Listener {

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        final Location location = event.getLocation();
        final World world;
        try {
            world = location.getWorld();
        } catch (IllegalArgumentException e) {
            //World unloaded
            return;
        }
        if (world == null) return;
        final Database database = new Database();
        final Iterator<Block> blocks = event.blockList().iterator();
        try {
            final Block min = location.getBlock().getRelative(-10, -10, -10);
            final Block max = location.getBlock().getRelative(10, 10, 10);

            final String sql = "SELECT x, y, z, world FROM timemine"
                    + "WHERE x >= " + min.getX() + " AND x <= " + max.getX()
                    + "AND   y >= " + min.getY() + " AND y <= " + max.getY()
                    + "AND   z >= " + min.getZ() + " AND z <= " + max.getZ()
                    + "AND world = '" + world.getName() + "'";
            final ResultSet results = database.query(sql);
            final ArrayList<Block> existing = new ArrayList<Block>();
            if(results == null) return;

            while (results.next()) {
                final Block block = world.getBlockAt(results.getInt("x"), results.getInt("y"), results.getInt("z"));
                existing.add(block);
            }
            while (blocks.hasNext()) {
                final Block block = blocks.next();
                if (existing.contains(block)) blocks.remove();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.closeConnection();
        }
    }
}
