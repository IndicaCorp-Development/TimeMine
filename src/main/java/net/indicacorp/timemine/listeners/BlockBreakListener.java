package net.indicacorp.timemine.listeners;

import net.indicacorp.timemine.TimeMine;
import net.indicacorp.timemine.models.TimeMineBlock;
import net.indicacorp.timemine.models.Tool;
import net.indicacorp.timemine.util.BlockCache;
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

import java.util.ArrayList;
import java.util.Random;

public class BlockBreakListener implements Listener {

    private static ArrayList<Material> TOOLS = new ArrayList<Material>(){{
        add(Material.DIAMOND_PICKAXE);
        add(Material.IRON_PICKAXE);
        add(Material.STONE_PICKAXE);
        add(Material.GOLDEN_PICKAXE);
    }};
    private static String prefix = TimeMine.getInstance().getConfig().getString("timemine.prefix");

    public BlockBreakListener() {}

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        int x = block.getX();
        int y = block.getY();
        int z = block.getZ();
        World world = player.getWorld();
        String comboId = x + "-" + y + "-" + z + "-" + world.getName();
        TimeMineBlock b = BlockCache.getBlock(comboId);

        //Doesn't exist so cancel
        if (b == null) return;

        //Cancel block break event and handle manually
        event.setCancelled(true);

        //If creative don't break block or collect drop item
        if (player.getGameMode().equals(GameMode.CREATIVE)) return;

        //Block isn't ready to be mined
        if (b.isMined()) {
            player.sendMessage(prefix + " This block can not be mined currently!");
            return;
        }

        //Check if tool being used is allowed
        Tool tool = new Tool(player.getInventory().getItemInMainHand().getType());
        if (!tool.canMine(block.getType())) {
            player.sendMessage(prefix + " You can't mine this with that.");
            return;
        }
        //Yes, so handle the break event, and drop the set item stack
        String d = b.getDropItemCount();
        int dropCount;
        if (!d.contains("-")) {
            dropCount = Integer.parseInt(b.getDropItemCount());
        } else {
            int min = Integer.parseInt(d.split("-")[0]);
            int max = Integer.parseInt(d.split("-")[1]);
            dropCount = new Random().nextInt((max - min) + 1) + min;
        }
        block.getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(b.getDropItem(), dropCount));

        //Block is ready to be mined... Continue with event
        //Update isMined, minedAt, timestamp, and set block to originalBlock
        b.setMined(true);
        b.updateMinedAt();
        block.setType(b.getOriginalBlock());

        //Update damage values of tool in hand used to mine block
        ItemStack handItem = player.getInventory().getItemInMainHand();
        ItemMeta meta = handItem.getItemMeta();
        if(meta instanceof Damageable) {
            ((Damageable) meta).setDamage(((Damageable) meta).getDamage() + 1);
            handItem.setItemMeta(meta);
        }
    }
}
