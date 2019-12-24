package net.indicacorp.timemine.util;

import net.indicacorp.timemine.TimeMine;
import net.indicacorp.timemine.exceptions.BlockNotFoundException;
import net.indicacorp.timemine.exceptions.InvalidWorldException;
import net.indicacorp.timemine.models.TimeMineBlock;
import net.indicacorp.timemine.tasks.BlockResetTask;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class CommandHandler implements CommandExecutor {
    TimeMine plugin;
    String prefix;

    public CommandHandler(TimeMine instance) {
        plugin = instance;
        prefix = plugin.getConfig().getString("timemine.prefix");
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (commandSender instanceof Player) {
            if (!commandSender.hasPermission("timemine.admin")) {
                commandSender.sendMessage(prefix + " You don't have permission to use this command.");
                return true;
            }
            if (args.length < 1) {
                sendHelp(commandSender);
                return true;
            }
            final String subCommand = args[0];
            switch (subCommand) {
                case "remove":
                case "delete":
                    handleRemove(commandSender, false);
                    break;
                case "removeall":
                case "deleteall":
                    handleRemove(commandSender, true);
                    break;
                case "stop":
                    plugin.stopResetTask();
                    commandSender.sendMessage("BlockResetTask has been stopped.");
                    break;
                case "start":
                    plugin.startResetTask();
                    commandSender.sendMessage("BlockResetTask has been started.");
                    break;
                case "list":
                    handleList(commandSender);
                    break;
                case "info":
                    handleInfo(commandSender);
                    break;
                case "help":
                    sendHelp(commandSender);
                    break;
                case "reset":
                    BlockCache.resetAllBlocks();
                    break;
                default:
                    handleAdd(commandSender, args);
                    break;
            }
        } else {
            plugin.getLogger().info("You may not use this command from console.");
        }
        return true;
    }

    private void sendHelp(CommandSender commandSender) {
        String str = prefix + " help:\n" +
                ChatColor.AQUA + "/timemine <display_block> <expires_after (number of seconds)> <drop_item> [drop_item_count (default: 1)]: " + ChatColor.RESET + "Create a TimeMine block at the targeted location.\n" +
                ChatColor.YELLOW + "Example: /timemine DIAMOND_ORE 20 DIAMOND 16\n" +
                ChatColor.AQUA + "/timemine list: " + ChatColor.RESET + "List all active TimeMine blocks.\n" +
                ChatColor.AQUA + "/timemine info: " + ChatColor.RESET + "List info of the currently targeted TimeMine block.\n" +
                ChatColor.AQUA + "/timemine help: " + ChatColor.RESET + "TimeMine help command.\n" +
                ChatColor.AQUA + "/timemine start: " + ChatColor.RESET + "Start/restart the BlockResetTask.\n" +
                ChatColor.AQUA + "/timemine stop: " + ChatColor.RESET + "Stop the BlockResetTask.\n" +
                ChatColor.AQUA + "/timemine delete | remove: " + ChatColor.RESET + "Removes the currently targeted TimeMine block.\n" +
                ChatColor.AQUA + "/timemine deleteall | removeall: " + ChatColor.RESET + "Removes all currently active TimeMine blocks.\n" +
                ChatColor.AQUA + "/timemine reset: " + ChatColor.RESET + "Resets all TimeMine blocks.";
        commandSender.sendMessage(str);
    }

    private void handleInfo(CommandSender commandSender) {
        Player player = (Player) commandSender;
        Block targetBlock = player.getTargetBlockExact(30, FluidCollisionMode.ALWAYS);

        //Check if target block is valid
        if(targetBlock == null) {
            player.sendMessage(prefix + " The block you are looking at is invalid or too far away.");
            return;
        }

        int x = targetBlock.getX();
        int y = targetBlock.getY();
        int z = targetBlock.getZ();
        World world = targetBlock.getWorld();
        String comboId = x + "-" + y + "-" + z + "-" + world.getName();
        TimeMineBlock b = BlockCache.getBlock(comboId);

        //Check if target block is TimeMine block
        if (b == null) {
            player.sendMessage(prefix + " You need to look at the TimeMine block in order to show its info.");
            return;
        }

        //Everything checks out... Show info
        String blockInfoString = ""
                + prefix + " block info:"
                + "\n" + ChatColor.RESET + "Coordinates: " + ChatColor.AQUA + world.getName() + ChatColor.AQUA + " X" + ChatColor.GREEN + x + ChatColor.AQUA + " Y" + ChatColor.GREEN + y + ChatColor.AQUA + " Z" + ChatColor.GREEN + z
                + "\n" + ChatColor.RESET + "Is Mined: " + ChatColor.AQUA + b.isMined()
                + "\n" + ChatColor.RESET + "Display Block: " + ChatColor.AQUA + b.getDisplayBlock().toString()
                + "\n" + ChatColor.RESET + "Original Block: " + ChatColor.AQUA + b.getOriginalBlock().toString()
                + "\n" + ChatColor.RESET + "Drop Item: " + ChatColor.AQUA + b.getDropItem().toString() + "x" + b.getDropItemCount()
                + "\n" + ChatColor.RESET + "Expires After: " + ChatColor.AQUA + b.getResetInterval() + " seconds";
        player.sendMessage(blockInfoString);
    }

    private void handleRemove(CommandSender commandSender, final boolean all) {
        Player player = (Player) commandSender;
        Block targetBlock = player.getTargetBlockExact(30, FluidCollisionMode.ALWAYS);

        //Stop reset task while removing to prevent SQLExceptions
        plugin.stopResetTask();

        if (all) {
            BlockCache.removeAllBlocks();
        } else {
            //Check if target block is valid
            if(targetBlock == null) {
                player.sendMessage(prefix + " The block you are looking at is invalid or too far away.");
            } else {
                int x = targetBlock.getX();
                int y = targetBlock.getY();
                int z = targetBlock.getZ();
                World world = targetBlock.getWorld();
                String comboId = x + "-" + y + "-" + z + "-" + world.getName();
                try {
                    BlockCache.removeBlock(comboId);
                } catch (BlockNotFoundException e) {
                    player.sendMessage(prefix + " The target block is not configured as a TimeMine block.");
                }
            }
        }

        //Resume reset task
        plugin.startResetTask();
    }

    private void handleList(CommandSender commandSender) {
        Player player = (Player) commandSender;
        HashMap<String, TimeMineBlock> cache = BlockCache.getCache();

        int count = 1;
        String message =  prefix + " Active Blocks:";
        for (Map.Entry<String, TimeMineBlock> entry : cache.entrySet()) {
            TimeMineBlock b = entry.getValue();
            message += ChatColor.RESET + "\n#" + count + " : " + ChatColor.AQUA + b.world.getName() + ChatColor.AQUA + " X" + ChatColor.GREEN + b.x + ChatColor.AQUA + " Y" + ChatColor.GREEN + b.y + ChatColor.AQUA + " Z" + ChatColor.GREEN + b.z;
            count++;
        }
        player.sendMessage(message);
    }

    private void handleAdd(CommandSender commandSender, String[] args) {
        Player player = (Player) commandSender;
        Block targetBlock = player.getTargetBlockExact(30, FluidCollisionMode.ALWAYS);

        //Check for all required arguments
        if (args.length < 3) {
            player.sendMessage(prefix + " You have not provided all of the required parameters for this command.\nCommand Usage: /timemine <display_block> <interval> <drop_item> [drop_item_count (default: 1)]");
            return;
        }

        Material displayBlock = Material.matchMaterial(args[0]);
        Material dropItem = Material.matchMaterial(args[2]);

        //Try to parse provided integers
        short resetInterval;
        short dropItemCount = 1;
        try {
            resetInterval = Short.parseShort(args[1]);
        } catch(NumberFormatException e) {
            player.sendMessage(prefix + " Invalid integer provided for resetInterval.");
            return;
        }
        if (args.length > 3) {
            try {
                dropItemCount = Short.parseShort(args[3]);
            } catch(NumberFormatException e) {
                player.sendMessage(prefix + " Invalid integer provided for dropItemCount.");
                return;
            }
        }

        //Validate provided arguments
        if(targetBlock == null) {
            player.sendMessage(prefix + " The block you are looking at is invalid or too far away.");
        } else if(displayBlock == null || !displayBlock.isBlock()) {
            player.sendMessage(prefix + " The specified display block is invalid.");
        } else if(dropItem == null) {
            player.sendMessage(prefix + " The specified drop item is invalid.");
        } else if(resetInterval < 1 || resetInterval >= 32767) {
            player.sendMessage(prefix + " Reset interval must be between 1 and 32767 (9.1 hours) seconds.");
        } else if(dropItemCount < 1 || dropItemCount > 64) {
            player.sendMessage(prefix + " Drop item count must be between 1 and 64.");
        } else {
            int x = targetBlock.getX();
            int y = targetBlock.getY();
            int z = targetBlock.getZ();
            World world = player.getWorld();
            String comboId = x + "-" + y + "-" + z + "-" + world.getName();
            TimeMineBlock b = BlockCache.getBlock(comboId);

            //Check if TimeMineBlock exists and update it
            if (b != null) {
                b.setMined(false);
                b.setDisplayBlock(displayBlock);
                b.setDropItem(dropItem);
                b.setDropItemCount(dropItemCount);
                b.setResetInterval(resetInterval);
                player.sendMessage(prefix + " Block successfully updated.");
            } else {
                //If block doesn't exist, create it
                try {
                    b = BlockCache.addBlock(targetBlock, displayBlock, dropItem, dropItemCount, resetInterval);
                    player.sendMessage(prefix + " Block successfully created.");
                } catch (SQLException | InvalidWorldException e) {
                    e.printStackTrace();
                    player.sendMessage("An error occurred while adding this TimeMine block.");
                    return;
                }
            }

            //Set the block type to its display block
            b.setBlockType(b.getDisplayBlock());
        }
    }
}
