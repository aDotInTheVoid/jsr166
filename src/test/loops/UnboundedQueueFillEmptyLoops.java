/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

import java.util.Random;
import java.util.Queue;

public class UnboundedQueueFillEmptyLoops {
    static int maxSize = 50000;
    static Random rng = new Random(3153122688L);
    static volatile int total;
    static Integer[] numbers;

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.printf(
                "Usage: UnboundedQueueFillEmptyLoops className [maxSize]%n");
            System.exit(1);
        }

        final Class<?> klass;
        try {
            klass = Class.forName(args[0]);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Class " + args[0] + " not found.");
        }

        if (args.length > 1)
            maxSize = Integer.parseInt(args[1]);

        System.out.printf("Class: %s size: %d%n", klass.getName(), maxSize);

        numbers = new Integer[maxSize];
        for (int i = 0; i < maxSize; ++i)
            numbers[i] = rng.nextInt(128);

        oneRun(klass, maxSize);
        Thread.sleep(100);
        oneRun(klass, maxSize);
        Thread.sleep(100);
        oneRun(klass, maxSize);

        if (total == 0) System.out.print(" ");
    }

    static void oneRun(Class<?> klass, int n) throws Exception {
        Queue<Integer> q =
            (Queue<Integer>) klass.getConstructor().newInstance();
        int sum = total;
        int m = rng.nextInt(numbers.length);
        long startTime = System.nanoTime();
        for (int k = 0; k < n; ++k) {
            for (int i = 0; i < k; ++i) {
                if (m >= numbers.length)
                    m = 0;
                q.offer(numbers[m++]);
            }
            for (Integer p; (p = q.poll()) != null; )
                sum += p.intValue();
        }
        total += sum;
        long endTime = System.nanoTime();
        long time = endTime - startTime;
        double secs = (double) time / 1000000000.0;
        System.out.println("Time: " + secs);
    }

}
