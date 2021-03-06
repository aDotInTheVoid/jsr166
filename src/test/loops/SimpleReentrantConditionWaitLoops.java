/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A variant of SimpleReentrantLockLoops that also has a very short timed wait.
 * Demonstrates 2% performance improvement on x86 with weakened atomics in AQS.
 */
public final class SimpleReentrantConditionWaitLoops {
    static final ExecutorService pool = Executors.newCachedThreadPool();
    static final LoopHelpers.SimpleRandom rng = new LoopHelpers.SimpleRandom();
    static final int ITERS = Integer.getInteger("iters", 2000000);
    static final int MAX_THREADS = Integer.getInteger("maxThreads", 1);
    static boolean print = false;

    public static void main(String[] args) throws Exception {

        new ReentrantConditionWaitLoop(1).test();
        new ReentrantConditionWaitLoop(1).test();
        print = true;

        for (int k = 1, i = 1; i <= MAX_THREADS;) {
            System.out.print("Threads: " + i);
            new ReentrantConditionWaitLoop(i).test();
            Thread.sleep(10);
            if (i == k) {
                k = i << 1;
                i = i + (i >>> 1);
            }
            else
                i = k;
        }
        pool.shutdown();
        if (!pool.awaitTermination(10, TimeUnit.SECONDS))
            throw new Error("pool failed to terminate!");
    }

    static final class ReentrantConditionWaitLoop implements Runnable {
        private int v = rng.next();
        private volatile int result = 17;
        private final ReentrantLock lock = new ReentrantLock();
        private final LoopHelpers.BarrierTimer timer = new LoopHelpers.BarrierTimer();
        private final CyclicBarrier barrier;
        private final int nthreads;
        private volatile int readBarrier;
        ReentrantConditionWaitLoop(int nthreads) {
            this.nthreads = nthreads;
            barrier = new CyclicBarrier(nthreads+1, timer);
        }

        final void test() throws Exception {
            for (int i = 0; i < nthreads; ++i)
                pool.execute(this);
            barrier.await();
            barrier.await();
            if (print) {
                long time = timer.getTime();
                long tpi = time / ((long) ITERS * nthreads);
                System.out.print("\t" + LoopHelpers.rightJustify(tpi) + " ns per lock");
                double secs = (double) time / 1000000000.0;
                System.out.println("\t " + secs + "s run time");
            }

            int r = result;
            if (r == 0) // avoid overoptimization
                System.out.println("useless result: " + r);
        }

        public final void run() {
            final ReentrantLock lock = this.lock;
            final Condition condition = lock.newCondition();
            try {
                barrier.await();
                int sum = v + 1;
                int x = 0;
                int n = ITERS;
                while (n-- > 0) {
                    lock.lock();
                    condition.await(10, TimeUnit.NANOSECONDS);
                    int k = (sum & 3);
                    if (k > 0) {
                        x = v;
                        while (k-- > 0)
                            x = LoopHelpers.compute6(x);
                        v = x;
                    }
                    else x = sum + 1;
                    lock.unlock();
                    if ((x += readBarrier) == 0)
                        ++readBarrier;
                    for (int l = x & 7; l > 0; --l)
                        sum += LoopHelpers.compute6(sum);
                }
                barrier.await();
                result += sum;
            }
            catch (Exception ie) {
                return;
            }
        }
    }

}
