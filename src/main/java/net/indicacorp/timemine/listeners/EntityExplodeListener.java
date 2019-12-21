package net.indicacorp.timemine.listeners;

import net.indicacorp.timemine.models.TimeMineBlock;
import net.indicacorp.timemine.util.BlockCache;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.HashMap;
import java.util.Iterator;

public class EntityExplodeListener implements Listener {

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        Location location = event.getLocation();
        World world;
        try {
            //Nullable and can throw an exception :facepalm:
            world = location.getWorld();
        } catch (IllegalArgumentException e) {
            //World unloaded
            return;
        }
        if (world == null) return;

        //Get exploded block list
        Iterator<Block> blocks = event.blockList().iterator();

        //Get min and max block radius around explosion (10 blocks should be enough I hope)
        Block min = location.getBlock().getRelative(-10, -10, -10);
        Block max = location.getBlock().getRelative(10, 10, 10);

        HashMap<String, TimeMineBlock> cache = BlockCache.getCache();

        //Check for existing key in cache and remove if exists
        while (blocks.hasNext()) {
            Block block = blocks.next();
            int x = block.getX();
            int y = block.getY();
            int z = block.getZ();
            String comboId = x + "-" + y + "-" + z + "-" + world.getName();
            if (cache.containsKey(comboId)) blocks.remove();
        }
    }
}
