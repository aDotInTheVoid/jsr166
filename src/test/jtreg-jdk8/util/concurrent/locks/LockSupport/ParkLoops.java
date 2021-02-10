/*
 * Written by Martin Buchholz with assistance from members of JCP
 * JSR-166 Expert Group and released to the public domain, as
 * explained at http://creativecommons.org/publicdomain/zero/1.0/
 */

/*
 * @test
 * @bug 8074773
 * @summary Stress test looks for lost unparks
 * @library /lib/testlibrary/
 * @modules java.management
 * @run main/timeout=1200 ParkLoops
 */

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.util.SplittableRandom;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.locks.LockSupport;
import jdk.testlibrary.Utils;

public final class ParkLoops {
    static final long TEST_TIMEOUT_SECONDS = Utils.adjustTimeout(1000);
    static final long LONG_DELAY_MS = Utils.adjustTimeout(10_000);
    static final int THREADS = 4;
    static final int ITERS = 30_000;

    static class Parker implements Runnable {
        static {
            // Reduce the risk of rare disastrous classloading in first call to
            // LockSupport.park: https://bugs.openjdk.java.net/browse/JDK-8074773
            Class<?> ensureLoaded = LockSupport.class;
        }

        private final AtomicReferenceArray<Thread> threads;
        private final CountDownLatch done;
        private final SplittableRandom rnd;

        Parker(AtomicReferenceArray<Thread> threads,
                 CountDownLatch done,
                 SplittableRandom rnd) {
            this.threads = threads; this.done = done; this.rnd = rnd;
        }

        public void run() {
            final Thread current = Thread.currentThread();
            for (int k = ITERS, j; k > 0; k--) {
                do {
                    j = rnd.nextInt(THREADS);
                } while (!threads.compareAndSet(j, null, current));
                do {                    // handle spurious wakeups
                    LockSupport.park();
                } while (threads.get(j) == current);
            }
            done.countDown();
        }
    }

    static class Unparker implements Runnable {
        static {
            // Reduce the risk of rare disastrous classloading in first call to
            // LockSupport.park: https://bugs.openjdk.java.net/browse/JDK-8074773
            Class<?> ensureLoaded = LockSupport.class;
        }

        private final AtomicReferenceArray<Thread> threads;
        private final CountDownLatch done;
        private final SplittableRandom rnd;

        Unparker(AtomicReferenceArray<Thread> threads,
                 CountDownLatch done,
                 SplittableRandom rnd) {
            this.threads = threads; this.done = done; this.rnd = rnd;
        }

        public void run() {
            for (int n = 0; (n++ & 0xff) != 0 || done.getCount() > 0;) {
                int j = rnd.nextInt(THREADS);
                Thread parker = threads.get(j);
                if (parker != null &&
                    threads.compareAndSet(j, parker, null)) {
                    LockSupport.unpark(parker);
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        final SplittableRandom rnd = new SplittableRandom();
        final ExecutorService pool = Executors.newCachedThreadPool();
        final AtomicReferenceArray<Thread> threads
            = new AtomicReferenceArray<>(THREADS);
        final CountDownLatch done = new CountDownLatch(THREADS);
        final Runnable parker = new Parker(threads, done, rnd.split());
        final Runnable unparker = new Unparker(threads, done, rnd.split());
        for (int i = 0; i < THREADS; i++) {
            pool.submit(parker);
            pool.submit(unparker);
        }
        try {
          if (!done.await(TEST_TIMEOUT_SECONDS, SECONDS)) {
            dumpAllStacks();
            throw new AssertionError("lost unpark");
          }
        } finally {
          pool.shutdown();
          pool.awaitTermination(LONG_DELAY_MS, MILLISECONDS);
        }
    }

    static void dumpAllStacks() {
        ThreadInfo[] threadInfos =
            ManagementFactory.getThreadMXBean().dumpAllThreads(true, true);
        for (ThreadInfo threadInfo : threadInfos) {
            System.err.print(threadInfo);
        }
    }
}
