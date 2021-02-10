/*
 * Written by Doug Lea and Martin Buchholz with assistance from
 * members of JCP JSR-166 Expert Group and released to the public
 * domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

/*
 * @test
 * @bug 8004138
 * @modules java.base/java.util.concurrent:open
 * @summary Checks that ForkJoinTask thrown exceptions are not leaked.
 * This whitebox test is sensitive to forkjoin implementation details.
 */

import static java.util.concurrent.TimeUnit.SECONDS;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.function.BooleanSupplier;

public class FJExceptionTableLeak {
    static class FailingTaskException extends RuntimeException {}
    static class FailingTask extends RecursiveAction {
        public void compute() { throw new FailingTaskException(); }
    }

    static int bucketsInuse(Object[] exceptionTable) {
        int count = 0;
        for (Object x : exceptionTable)
            if (x != null) count++;
        return count;
    }

    public static void main(String[] args) throws Exception {
        final ForkJoinPool pool = new ForkJoinPool(4);
        final Field exceptionTableField =
            ForkJoinTask.class.getDeclaredField("exceptionTable");
        exceptionTableField.setAccessible(true);
        final Object[] exceptionTable = (Object[]) exceptionTableField.get(null);

        if (bucketsInuse(exceptionTable) != 0) throw new AssertionError();

        final ArrayList<FailingTask> tasks = new ArrayList<>();

        // Keep submitting failing tasks until most of the exception
        // table buckets are in use
        do {
            for (int i = 0; i < exceptionTable.length; i++) {
                FailingTask task = new FailingTask();
                pool.execute(task);
                tasks.add(task); // retain strong refs to all tasks, for now
            }
            for (FailingTask task : tasks) {
                try {
                    task.join();
                    throw new AssertionError("should throw");
                } catch (FailingTaskException success) {}
            }
        } while (bucketsInuse(exceptionTable) < exceptionTable.length * 3 / 4);

        // Retain a strong ref to one last failing task;
        // task.join() will trigger exception table expunging.
        FailingTask lastTask = tasks.get(0);

        // Clear all other strong refs, making exception table cleanable
        tasks.clear();

        BooleanSupplier exceptionTableIsClean = () -> {
            try {
                lastTask.join();
                throw new AssertionError("should throw");
            } catch (FailingTaskException expected) {}
            int count = bucketsInuse(exceptionTable);
            if (count == 0)
                throw new AssertionError("expected to find last task");
            return count == 1;
        };
        gcAwait(exceptionTableIsClean);
    }

    // --------------- GC finalization infrastructure ---------------

    /** No guarantees, but effective in practice. */
    static void forceFullGc() {
        CountDownLatch finalizeDone = new CountDownLatch(1);
        WeakReference<?> ref = new WeakReference<Object>(new Object() {
            protected void finalize() { finalizeDone.countDown(); }});
        try {
            for (int i = 0; i < 10; i++) {
                System.gc();
                if (finalizeDone.await(1L, SECONDS) && ref.get() == null) {
                    System.runFinalization(); // try to pick up stragglers
                    return;
                }
            }
        } catch (InterruptedException unexpected) {
            throw new AssertionError("unexpected InterruptedException");
        }
        throw new AssertionError("failed to do a \"full\" gc");
    }

    static void gcAwait(BooleanSupplier s) {
        for (int i = 0; i < 10; i++) {
            if (s.getAsBoolean())
                return;
            forceFullGc();
        }
        throw new AssertionError("failed to satisfy condition");
    }
}
