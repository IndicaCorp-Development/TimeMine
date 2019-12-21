package net.indicacorp.timemine.tasks;

import net.indicacorp.timemine.models.TimeMineBlock;
import net.indicacorp.timemine.util.BlockCache;

import java.util.*;
//import java.util.concurrent.Executors;
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.TimeUnit;

public class BlockResetTask {
//    private ScheduledExecutorService executorService;
    private Timer timer;
    private int period;
    private boolean paused;

    public BlockResetTask(int interval) {
//        executorService = Executors.newSingleThreadScheduledExecutor();
        period = interval;
        paused = false;
        timer = new Timer();
    }

    public void stop() { paused = true; }
    public void start() { paused = false; }

//    public void initTask() {
//        executorService.scheduleAtFixedRate(() -> {
//
//        }, 0, period, TimeUnit.SECONDS);
//    }
    public void initTask() {
        timer.scheduleAtFixedRate(new Task(), 0, period*1000);
    }

    private class Task extends TimerTask {
        @Override
        public void run() {
            if (paused) return;
            paused = true;

            System.out.println("Run Block Reset task");

            HashMap<String, TimeMineBlock> cache = BlockCache.getCache();

            for (Map.Entry<String, TimeMineBlock> entry : cache.entrySet()) {
                TimeMineBlock b = entry.getValue();
                if (!b.isMined()) continue;
                int resetInterval = b.getResetInterval();
                Date minedAt = b.getMinedAt();
                Date expiredAt = new Date(minedAt.getTime() + (resetInterval * 1000));
                Date currentDate = new Date();

                System.out.println("TimeMine Block Task Should Run: " + (expiredAt.getTime() <= currentDate.getTime()));

                if (expiredAt.getTime() <= currentDate.getTime()) {
                    b.setMined(false);
                    b.clearMinedAt();
                    b.setBlockType(b.getDisplayBlock());
                }
            }

            paused = false;
        }
    }
}
