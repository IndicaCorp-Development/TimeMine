package net.indicacorp.timemine;

import net.indicacorp.timemine.listeners.BlockBreakListener;
import net.indicacorp.timemine.listeners.EntityExplodeListener;
import net.indicacorp.timemine.models.Tool;
import net.indicacorp.timemine.tasks.BlockResetTask;
import net.indicacorp.timemine.util.CommandHandler;
import net.indicacorp.timemine.util.Database;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.ResultSet;

public class TimeMine extends JavaPlugin {

    private static TimeMine instance;
    private static Database database;
    private static BlockResetTask blockResetTask;

    @Override
    public void onEnable() {
        super.onEnable();
        instance = this;
        database = new Database();
        blockResetTask = new BlockResetTask(this, getConfig().getInt("timemine.block_reset_interval"));

        //Save default config.yml if not exists
        saveDefaultConfig();

        //Check for database driver
        if (!database.checkDatabaseDriver()) {
            getLogger().warning("Database driver is not available for connections.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        try {
            //Init database schema
            database.initDatabase();

            //Reset all TimeMine blocks
            initAllBlocks();

            //Start reset task thread
            blockResetTask.start();

            //Load main command handler
            getCommand("timemine").setExecutor(new CommandHandler(this));

            //Register listeners
            getServer().getPluginManager().registerEvents(new BlockBreakListener(), this);
            getServer().getPluginManager().registerEvents(new EntityExplodeListener(), this);

            getLogger().info("Plugin loaded successfully.");

            new Tool(Material.getMaterial("DIAMOND_PICKAXE"));
        } catch(Exception e) {
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        blockResetTask.stop();
        database.closeConnection();
        getLogger().info("Plugin disabled successfully.");
    }

    public void disablePlugin() {
        getServer().getPluginManager().disablePlugin(this);
    }
    public void stopBlockResetTask() { blockResetTask.stop(); }
    public void startBlockResetTask() { blockResetTask.start(); }

    public void initAllBlocks() {
        try {
            String sql = "SELECT * FROM timemine";
            final ResultSet results = database.query(sql);
            if (results != null) {
                while (results.next()) {
                    final int x = results.getInt("x");
                    final int y = results.getInt("y");
                    final int z = results.getInt("z");
                    final World world = Bukkit.getServer().getWorld(results.getString("world"));
                    final Block block = world.getBlockAt(x, y, z);
                    final Material displayBlock = Material.getMaterial(results.getString("displayBlock"));
                    block.setType(displayBlock);
                    sql = "UPDATE timemine SET minedAt = NULL, isMined = 0 WHERE x = " + x + " AND y = " + y + " AND z = " + z + " AND world = " + world.getName();
                    database.insertOrUpdate(sql);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.closeConnection();
        }
    }

    public static TimeMine getInstance(){
        return instance;
    }
}
