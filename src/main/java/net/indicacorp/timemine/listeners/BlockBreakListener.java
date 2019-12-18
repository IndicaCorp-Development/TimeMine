package net.indicacorp.timemine.listeners;

import net.indicacorp.timemine.TimeMine;
import net.indicacorp.timemine.util.Database;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.ResultSet;
import java.util.ArrayList;

public class BlockBreakListener implements Listener {

    private final TimeMine plugin;
    private static ArrayList<Material> TOOLS;

    public BlockBreakListener() {
        plugin = TimeMine.getInstance();
        TOOLS = new ArrayList<Material>(){{
            add(Material.DIAMOND_PICKAXE);
            add(Material.IRON_PICKAXE);
            add(Material.STONE_PICKAXE);
            add(Material.GOLDEN_PICKAXE);
        }};
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        final Database database = new Database();

        try {
            Player player = event.getPlayer();
            Block block = event.getBlock();
            int x = block.getX();
            int y = block.getY();
            int z = block.getZ();
            World world = player.getWorld();
            String sql = "SELECT * FROM timemine WHERE x = " + x + " AND y = " + y + " AND z = " + z + " AND world = '" + world.getName() + "' LIMIT 1";
            ResultSet results = database.query(sql);
            if (results == null || !results.first())
                return;

            event.setCancelled(true);
            if (player.getGameMode().equals(GameMode.CREATIVE))
                return;

            Material originalBlock = Material.getMaterial(results.getString("originalBlock"));
            Material dropItem = Material.getMaterial(results.getString("dropItem"));
            int dropItemCount = results.getInt("dropItemCount");
            boolean isMined = results.getBoolean("isMined");

            if (isMined) {
                player.sendMessage("This block can not be mined currently!");
                return;
            }

            if (TOOLS.contains(player.getInventory().getItemInMainHand().getType())) {
                block.getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(dropItem, dropItemCount));
            }
            sql = "UPDATE timemine SET isMined = 1, minedAt = CURRENT_TIMESTAMP WHERE x = " + x + " AND y = " + y + " AND z = " + z + " AND world = '" + world.getName() + "'";
            database.insertOrUpdate(sql);
            block.setType(originalBlock);
            ItemStack handItem = player.getInventory().getItemInMainHand();
            ItemMeta meta = handItem.getItemMeta();
            if(meta instanceof Damageable) {
                ((Damageable) meta).setDamage(((Damageable) meta).getDamage() + 1);
                handItem.setItemMeta(meta);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.closeConnection();
        }
    }
}
