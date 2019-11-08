package com.hedera.hashgraph.sdk;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

final class ThreadUtil {
    private ThreadUtil() { }

    static void sleepDuration(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    static void schedule(Runnable runnable, Duration delay) {
        scheduledExecutor.schedule(runnable, delay.toMillis(), TimeUnit.MILLISECONDS);
    }

    // we need a background thread to execute timeouts
    private static final ScheduledExecutorService scheduledExecutor =
        Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "hedera-async-executor"));
}
