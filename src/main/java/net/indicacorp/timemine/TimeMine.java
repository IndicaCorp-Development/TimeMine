package net.indicacorp.timemine;

import net.indicacorp.timemine.exceptions.CommandNotFoundException;
import net.indicacorp.timemine.listeners.BlockBreakListener;
import net.indicacorp.timemine.listeners.EntityExplodeListener;
import net.indicacorp.timemine.tasks.BlockResetTask;
import net.indicacorp.timemine.util.BlockCache;
import net.indicacorp.timemine.util.CommandHandler;
import net.indicacorp.timemine.util.DatabaseHelper;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

public class TimeMine extends JavaPlugin {

    BlockResetTask blockResetTask;
    FileConfiguration config;
    static TimeMine instance;

    public TimeMine() {
        instance = this;
        blockResetTask = new BlockResetTask(getConfig().getInt("timemine.expired_block_reset_interval"));
        config = getConfig();
    }

    public static TimeMine getInstance(){
        return instance;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        config.options().copyDefaults(true);
        saveConfig();
        initPlugin();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        blockResetTask.cancelTask();
    }

    public void disablePlugin() { this.getServer().getPluginManager().disablePlugin(this); }
    public void stopResetTask() { blockResetTask.stop(); }
    public void startResetTask() { blockResetTask.start(); }

    private void initPlugin() {
        try {
            //Initialize database helper and table
            DatabaseHelper.initDatabase();

            //Start block reset task
            blockResetTask.initTask();

            //Cache existing blocks
            BlockCache.cacheBlocks();

            //Load main command executor
            PluginCommand command = getCommand("timemine");
            if (command == null) throw new CommandNotFoundException();
            command.setExecutor(new CommandHandler(this));

            //Register listeners
            getServer().getPluginManager().registerEvents(new BlockBreakListener(), this);
            getServer().getPluginManager().registerEvents(new EntityExplodeListener(), this);
        } catch (CommandNotFoundException e) {
            getLogger().warning("Command not found in plugin.yml");
            disablePlugin();
        } catch (ClassNotFoundException e) {
            getLogger().warning("Database driver could not be found");
            disablePlugin();
        } catch (SQLException e) {
            getLogger().warning(e.getMessage());
            disablePlugin();
        }
    }
}
