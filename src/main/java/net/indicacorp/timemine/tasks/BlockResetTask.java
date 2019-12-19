package net.indicacorp.timemine.tasks;

import net.indicacorp.timemine.TimeMine;
import net.indicacorp.timemine.util.Database;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;

import java.sql.ResultSet;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BlockResetTask {
    private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    TimeMine plugin;
    private int period;
    private boolean inProgress = false;
    private Future<?> task = null;

    public BlockResetTask(TimeMine instance, int interval) {
        plugin = instance;
        period = interval;
    }

    public void stop() {
        if (task != null) task.cancel(true);
        task = null;
    }

    public void start() {
        if (task != null) task.cancel(true);
        task = executorService.scheduleAtFixedRate(new ResetTask(), 0, period, TimeUnit.SECONDS);
    }

    private class ResetTask implements Runnable {
        @Override
        public void run() {
            Database database = new Database();
            if (inProgress) return;
            inProgress = true;

            try {
                String sql = "SELECT * FROM timemine WHERE isMined = 1";
                final ResultSet results = database.query(sql);
                if (results != null) {
                    while (results.next()) {
                        final int x = results.getInt("x");
                        final int y = results.getInt("y");
                        final int z = results.getInt("z");
                        final World world = Bukkit.getServer().getWorld(results.getString("world"));
                        final Material displayBlock = Material.getMaterial(results.getString("displayBlock"));
                        final int resetInterval = results.getInt("resetInterval");
                        final Date minedAt = results.getTimestamp("minedAt");
                        final Date expiredAt = new Date(minedAt.getTime() + (resetInterval * 1000));
                        final Date currentDate = new Date();

                        if (world == null || displayBlock == null) return;

                        if (expiredAt.getTime() <= currentDate.getTime()) {
                            Bukkit.getScheduler().runTask(plugin, () -> world.getBlockAt(x, y, z).setType(displayBlock));
                            sql = "UPDATE timemine SET isMined = 0, minedAt = NULL WHERE x = " + x + " AND y = " + y + " AND z = " + z + " AND world = " + world.getName();
                            database.insertOrUpdate(sql);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                database.closeConnection();
                inProgress = false;
            }
        }
    }
}
