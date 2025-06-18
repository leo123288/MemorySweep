package com.tkisor.memorysweep.utils;

import com.tkisor.memorysweep.MemorySweep;
import com.tkisor.memorysweep.config.ModConfig;
import com.tkisor.memorysweep.task.MemoryUsageTask;
import org.apache.commons.lang3.tuple.Pair;

import java.text.DecimalFormat;
import java.util.Timer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

public class SweepUtil {
    private static final DecimalFormat DF = new DecimalFormat("#.##");
    private static long lastSweep = System.currentTimeMillis();
    private static long lastAvgSweep = System.currentTimeMillis();

    public static void sweep() {
        lastSweep = System.currentTimeMillis();
        Thread sweepThread = new Thread(() -> {
            try {
                long before = getUsedMemory();
                System.gc();
                Thread.sleep(100);
                long after = getUsedMemory();
                MemorySweep.logger.info("MemorySweep: GC executed. Before: {}MB, After: {}MB",
                        before / (1024 * 1024), after / (1024 * 1024));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                MemorySweep.logger.warn("Memory sweep interrupted.", e);
            }
        });

        sweepThread.setDaemon(true);
        sweepThread.setName("MemorySweep-Thread");
        sweepThread.start();
    }

    public static CompletableFuture<Pair<Long, Long>> sweepAsync() {
        lastSweep = System.currentTimeMillis();
        return CompletableFuture.supplyAsync(() -> {
            try {
                long before = getUsedMemory();
                System.gc();
                Thread.sleep(100);
                long after = getUsedMemory();
                MemorySweep.logger.info("MemorySweep: GC executed. Before: {}MB, After: {}MB",
                        before / (1024 * 1024), after / (1024 * 1024));
                return Pair.of(before, after);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                MemorySweep.logger.warn("Memory sweep interrupted.", e);
                return Pair.of(-1L, -1L);
            }
        });
    }

    // 计算十秒内存占用然后清理
    public static CompletableFuture<Pair<Double, Pair<Long, Long>>> calculateAndSweepMemory() {
        lastAvgSweep = System.currentTimeMillis();
        return CompletableFuture.supplyAsync(() -> {
            CountDownLatch latch = new CountDownLatch(1);
            MemoryUsageTask memoryUsageTask = new MemoryUsageTask(latch);
            Timer timer = new Timer(true);
            timer.scheduleAtFixedRate(memoryUsageTask, 0, 1000);

            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                MemorySweep.logger.error("Memory usage task interrupted");
            }

            double averageUsage = memoryUsageTask.getAverageUsage();
            if (averageUsage * 100 >= ModConfig.get().minMemoryUsage) {
                lastSweep = System.currentTimeMillis();
                System.gc();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            long beforeGC = getUsedMemory();

            long afterGC = getUsedMemory();
            MemorySweep.logger.info("MemorySweep: GC executed. Before: {}MB, After: {}MB",
                    beforeGC / (1024 * 1024), afterGC / (1024 * 1024));
            return Pair.of(averageUsage, Pair.of(beforeGC, afterGC));
        });
    }

    public static boolean isSweep() {
        long currentTime = System.currentTimeMillis();
        long timeDifference = currentTime - lastSweep;

        return timeDifference >= (ModConfig.get().sweepInterval * 1000L);
    }

    public static boolean isAvgSweep() {
        long currentTime = System.currentTimeMillis();
        long timeDifference = currentTime - lastAvgSweep;

        return timeDifference >= (3 * 60 * 1000L);
    }

    public static long getUsedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    public static double getMemoryInGB() {
        return Double.parseDouble(DF.format(SweepUtil.getUsedMemory() / 1024.0 / 1024.0 / 1024.0));
    }
}
