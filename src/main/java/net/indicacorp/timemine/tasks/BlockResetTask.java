package net.indicacorp.timemine.tasks;

import net.indicacorp.timemine.models.TimeMineBlock;
import net.indicacorp.timemine.util.BlockCache;

import java.util.*;

public class BlockResetTask {
    private Timer timer;
    private int period;
    private boolean paused;

    public BlockResetTask(int interval) {
        period = interval;
        paused = false;
        timer = new Timer();
    }

    public void stop() { paused = true; }
    public void start() { paused = false; }
    public void initTask() {
        timer.scheduleAtFixedRate(new Task(), 0, period*1000);
    }

    private class Task extends TimerTask {
        @Override
        public void run() {
            if (paused) return;
            paused = true;

            HashMap<String, TimeMineBlock> cache = BlockCache.getCache();

            for (Map.Entry<String, TimeMineBlock> entry : cache.entrySet()) {
                TimeMineBlock b = entry.getValue();
                if (!b.isMined()) continue;
                int resetInterval = b.getResetInterval();
                Date minedAt = b.getMinedAt();
                Date expiredAt = new Date(minedAt.getTime() + (resetInterval * 1000));
                Date currentDate = new Date();

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
