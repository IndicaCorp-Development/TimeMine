package net.indicacorp.timemine.util;

import net.indicacorp.timemine.TimeMine;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CommandHandler implements CommandExecutor {
    TimeMine plugin;
    final String prefix;

    public CommandHandler(TimeMine instance) {
        plugin = instance;
        prefix = plugin.getConfig().getString("timemine.prefix");
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (commandSender instanceof Player) {
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
                    plugin.stopBlockResetTask();
                    commandSender.sendMessage("BlockResetTask has been stopped.");
                    break;
                case "start":
                    plugin.startBlockResetTask();
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
                    plugin.initAllBlocks();
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
                ChatColor.YELLOW + "General usage: /timemine <display_block> <interval> <drop_item> [drop_item_count (default: 1)]\n" +
                ChatColor.AQUA + "/timemine list: " + ChatColor.RESET + "List all active TimeMine blocks.\n" +
                ChatColor.AQUA + "/timemine info: " + ChatColor.RESET + "List info of the currently targeted TimeMine block.\n" +
                ChatColor.AQUA + "/timemine help: " + ChatColor.RESET + "TimeMine help command.\n" +
                ChatColor.AQUA + "/timemine start: " + ChatColor.RESET + "Start/restart the BlockResetTask.\n" +
                ChatColor.AQUA + "/timemine stop: " + ChatColor.RESET + "Stop the BlockResetTask.\n" +
                ChatColor.AQUA + "/timemine delete | remove: " + ChatColor.RESET + "Removes the currently targeted TimeMine block.\n" +
                ChatColor.AQUA + "/timemine deleteall | removeall: " + ChatColor.RESET + "Removes all currently active TimeMine blocks.";
        commandSender.sendMessage(str);
    }

    private void handleInfo(CommandSender commandSender) {
        final Database database = new Database();
        final Player player = (Player) commandSender;
        final Block targetBlock = player.getTargetBlockExact(30, FluidCollisionMode.ALWAYS);
        if(targetBlock == null) {
            player.sendMessage(prefix + " The block you are looking at is invalid or too far away.");
            return;
        }
        final int x = targetBlock.getX();
        final int y = targetBlock.getY();
        final int z = targetBlock.getZ();
        final World world = targetBlock.getWorld();
        try {
            final String sql = "SELECT * FROM timemine WHERE x = " + x + " AND y = " + y + " AND z = " + z + " AND world = " + world.getName();
            ResultSet results = database.query(sql);
            if (results != null && results.first()) {
                final String blockInfoString = ""
                        + prefix + " block info:"
                        + "\n" + ChatColor.RESET + "Coordinates: " + ChatColor.AQUA + world.getName() + ChatColor.AQUA + " X" + ChatColor.GREEN + x + ChatColor.AQUA + " Y" + ChatColor.GREEN + y + ChatColor.AQUA + " Z" + ChatColor.GREEN + z
                        + "\n" + ChatColor.RESET + "Is Mined: " + ChatColor.AQUA + results.getBoolean("isMined")
                        + "\n" + ChatColor.RESET + "Display Block: " + ChatColor.AQUA + results.getString("displayBlock")
                        + "\n" + ChatColor.RESET + "Original Block: " + ChatColor.AQUA + results.getString("originalBlock")
                        + "\n" + ChatColor.RESET + "Drop Item: " + ChatColor.AQUA + results.getString("dropItem") + "x" + results.getInt("dropItemCount")
                        + "\n" + ChatColor.RESET + "Reset Interval: " + ChatColor.AQUA + results.getInt("resetInterval") + " seconds";
                player.sendMessage(blockInfoString);
            } else {
                player.sendMessage(prefix + " You need to look at the TimeMine block in order to show its info.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.closeConnection();
        }
    }

    private void handleRemove(CommandSender commandSender, final boolean all) {
        final Database database = new Database();
        final Player player = (Player) commandSender;
        //Stop reset task while removing
        plugin.stopBlockResetTask();
        try {
            String sql;
            if (all) {
                sql = "SELECT * FROM timemine";
                ResultSet results = database.query(sql);
                if (results != null) {
                    while (results.next()) {
                        final Material originalBlock = Material.getMaterial(results.getString("originalBlock"));
                        final int x = results.getInt("x");
                        final int y = results.getInt("y");
                        final int z = results.getInt("z");
                        final World world = Bukkit.getServer().getWorld(results.getString("world"));
                        //noinspection ConstantConditions
                        world.getBlockAt(x, y, z).setType(originalBlock);
                    }
                    sql = "TRUNCATE timemine";
                    database.insertOrUpdate(sql);
                    player.sendMessage(prefix + " All TimeMine blocks were removed.");
                } else {
                    player.sendMessage(prefix + " There are no TimeMine blocks to remove!");
                }
            } else {
                final Block targetBlock = player.getTargetBlockExact(30, FluidCollisionMode.ALWAYS);
                if(targetBlock == null) {
                    player.sendMessage(prefix + " The block you are looking at is invalid or too far away.");
                    return;
                }
                final int x = targetBlock.getX();
                final int y = targetBlock.getY();
                final int z = targetBlock.getZ();
                final World world = targetBlock.getWorld();

                sql = "SELECT * FROM timemine WHERE x = " + x + " AND y = " + y + " AND z = " + z + " AND world = " + world.getName();
                ResultSet results = database.query(sql);
                if (results != null && results.first()) {
                    final Material originalBlock = Material.getMaterial(results.getString("originalBlock"));
                    //noinspection ConstantConditions
                    targetBlock.setType(originalBlock);
                    sql = "DELETE FROM timemine WHERE x = " + x + " AND y = " + y + " AND z = " + z + " AND world = " + world.getName();
                    database.insertOrUpdate(sql);
                    player.sendMessage(prefix + " TimeMine block removed successfully!");
                } else {
                    player.sendMessage(prefix + " You need to look at the TimeMine block in order to remove it.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.closeConnection();
            //Resume reset task
            plugin.startBlockResetTask();
        }
    }

    private void handleList(CommandSender commandSender) {
        final Database database = new Database();
        final Player player = (Player) commandSender;
        StringBuilder message = new StringBuilder();
        message.append(prefix).append(" Active  Blocks:");
        try {
            final String sql = "SELECT x, y, z, world FROM timemine";
            final ResultSet results = database.query(sql);
            if (results != null) {
                int count = 1;
                while (results.next()) {
                    final int x = results.getInt("x");
                    final int y = results.getInt("y");
                    final int z = results.getInt("z");
                    final String world = results.getString("world");
                    final String str = ChatColor.RESET + "\n#" + count + " : " + ChatColor.AQUA + world + ChatColor.AQUA + " X" + ChatColor.GREEN + x + ChatColor.AQUA + " Y" + ChatColor.GREEN + y + ChatColor.AQUA + " Z" + ChatColor.GREEN + z;
                    message.append(str);
                    count++;
                }
                player.sendMessage(message.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.closeConnection();
        }
    }

    private void handleAdd(CommandSender commandSender, String[] args) {
        final Database database = new Database();
        final Player player = (Player) commandSender;
        final Block targetBlock = player.getTargetBlockExact(30, FluidCollisionMode.ALWAYS);
        if (args.length < 3) {
            player.sendMessage(prefix + " You have not provided all of the required parameters for this command. Use /timemine help");
            return;
        }
        Material displayBlock = Material.matchMaterial(args[0]);
        Material dropItem = Material.matchMaterial(args[2]);
        int resetInterval;
        int dropItemCount = 1;
        try {
            resetInterval = Integer.parseInt(args[1]);
        } catch(NumberFormatException e) {
            player.sendMessage(prefix + " Invalid integer provided for resetInterval.");
            return;
        }
        if (args.length > 3) {
            try {
                dropItemCount = Integer.parseInt(args[3]);
            } catch(NumberFormatException e) {
                player.sendMessage(prefix + " Invalid integer provided for dropItemCount.");
                return;
            }
        }

        if(targetBlock == null) {
            player.sendMessage(prefix + " The block you are looking at is invalid or too far away.");
        } else if(displayBlock == null || !displayBlock.isBlock()) {
            player.sendMessage(prefix + " The specified display block is invalid.");
        } else if(dropItem == null) {
            player.sendMessage(prefix + " The specified drop item is invalid.");
        } else if(resetInterval < 1 || resetInterval > 86400) {
            player.sendMessage(prefix + " Reset interval must be between 1 and 86400 (24 hours) seconds.");
        } else if(dropItemCount < 1 || dropItemCount > 64) {
            player.sendMessage(prefix + " Drop item count must be between 1 and 64.");
        } else {
            try {
                final int x = targetBlock.getX();
                final int y = targetBlock.getY();
                final int z = targetBlock.getZ();
                final String world = player.getWorld().getName();
                boolean updated = false;
                String sql = "SELECT * FROM timemine WHERE x = " + x + " AND y = " + y + " AND z = " + z + " AND world = '" + world + "' LIMIT 1;";
                ResultSet results = database.query(sql);

                if (results != null && results.first()) {
                    sql = "UPDATE timemine SET isMined = 0, displayBlock = '" + displayBlock.toString() + "', dropItem = '" + dropItem.toString() + "', dropItemCount = " + dropItemCount + ", minedAt = NULL, resetInterval = " + resetInterval + " WHERE x = " + x + " AND y = " + y + " AND z = " + z + " AND world = '" + world + "'";
                    updated = true;
                } else {
                    sql = "INSERT INTO timemine (x, y, z, world, displayBlock, originalBlock, dropItem, dropItemCount, resetInterval) VALUES (" + x + ", " + y + ", " + z + ", '" + world + "', '" + displayBlock.toString() + "', '" + targetBlock.getType().toString() + "', '" + dropItem.toString() + "', " + dropItemCount + ", " + resetInterval + ")";
                }
                database.insertOrUpdate(sql);
                targetBlock.setType(displayBlock);
                if (updated) {
                    player.sendMessage(prefix + " Existing block has been updated successfully.");
                } else {
                    player.sendMessage(prefix + " Block has been created successfully.");
                }
            } catch (SQLException e) {
                player.sendMessage(prefix + " An internal server error has occurred. Check console for more information.");
                e.printStackTrace();
            } catch (Exception e) {
                player.sendMessage(prefix + " An internal server error has occurred. Please check console for more information.");
                e.printStackTrace();
            } finally {
                database.closeConnection();
            }
        }
    }
}
