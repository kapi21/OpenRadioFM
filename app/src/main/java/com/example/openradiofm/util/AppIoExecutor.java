package com.example.openradiofm.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Cola global de trabajo en segundo plano (I/O, red, AIDL no bloqueante en el hilo UI).
 * Sustituye {@code new Thread(...).start()} dispersos para acotar hilos y evitar picos bajo zapping rápido.
 */
public final class AppIoExecutor {

    private static final int POOL_SIZE = Math.max(3, Math.min(6, Runtime.getRuntime().availableProcessors() + 2));

    private static final ExecutorService EXEC = Executors.newFixedThreadPool(POOL_SIZE, new ThreadFactory() {
        private final AtomicInteger seq = new AtomicInteger(0);

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "OpenRadioFM-io-" + seq.incrementAndGet());
            t.setDaemon(true);
            t.setPriority(Thread.NORM_PRIORITY - 1);
            return t;
        }
    });

    private AppIoExecutor() {}

    public static void execute(Runnable r) {
        if (r == null) return;
        EXEC.execute(r);
    }
}
