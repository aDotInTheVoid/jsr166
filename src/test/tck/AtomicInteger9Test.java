/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AtomicInteger9Test extends JSR166TestCase {
    public static void main(String[] args) {
        main(suite(), args);
    }
    public static Test suite() {
        return new TestSuite(AtomicInteger9Test.class);
    }

    /**
     * getPlain returns the last value set
     */
    public void testGetPlainSet() {
        AtomicInteger ai = new AtomicInteger(1);
        assertEquals(1, ai.getPlain());
        ai.set(2);
        assertEquals(2, ai.getPlain());
        ai.set(-3);
        assertEquals(-3, ai.getPlain());
    }

    /**
     * getOpaque returns the last value set
     */
    public void testGetOpaqueSet() {
        AtomicInteger ai = new AtomicInteger(1);
        assertEquals(1, ai.getOpaque());
        ai.set(2);
        assertEquals(2, ai.getOpaque());
        ai.set(-3);
        assertEquals(-3, ai.getOpaque());
    }

    /**
     * getAcquire returns the last value set
     */
    public void testGetAcquireSet() {
        AtomicInteger ai = new AtomicInteger(1);
        assertEquals(1, ai.getAcquire());
        ai.set(2);
        assertEquals(2, ai.getAcquire());
        ai.set(-3);
        assertEquals(-3, ai.getAcquire());
    }

    /**
     * get returns the last value setPlain
     */
    public void testGetSetPlain() {
        AtomicInteger ai = new AtomicInteger(1);
        assertEquals(1, ai.get());
        ai.setPlain(2);
        assertEquals(2, ai.get());
        ai.setPlain(-3);
        assertEquals(-3, ai.get());
    }

    /**
     * get returns the last value setOpaque
     */
    public void testGetSetOpaque() {
        AtomicInteger ai = new AtomicInteger(1);
        assertEquals(1, ai.get());
        ai.setOpaque(2);
        assertEquals(2, ai.get());
        ai.setOpaque(-3);
        assertEquals(-3, ai.get());
    }

    /**
     * get returns the last value setRelease
     */
    public void testGetSetRelease() {
        AtomicInteger ai = new AtomicInteger(1);
        assertEquals(1, ai.get());
        ai.setRelease(2);
        assertEquals(2, ai.get());
        ai.setRelease(-3);
        assertEquals(-3, ai.get());
    }

    /**
     * compareAndExchange succeeds in changing value if equal to
     * expected else fails
     */
    public void testCompareAndExchange() {
        AtomicInteger ai = new AtomicInteger(1);
        assertEquals(1, ai.compareAndExchange(1, 2));
        assertEquals(2, ai.compareAndExchange(2, -4));
        assertEquals(-4, ai.get());
        assertEquals(-4, ai.compareAndExchange(-5, 7));
        assertEquals(-4, ai.get());
        assertEquals(-4, ai.compareAndExchange(-4, 7));
        assertEquals(7, ai.get());
    }

    /**
     * compareAndExchangeAcquire succeeds in changing value if equal to
     * expected else fails
     */
    public void testCompareAndExchangeAcquire() {
        AtomicInteger ai = new AtomicInteger(1);
        assertEquals(1, ai.compareAndExchangeAcquire(1, 2));
        assertEquals(2, ai.compareAndExchangeAcquire(2, -4));
        assertEquals(-4, ai.get());
        assertEquals(-4, ai.compareAndExchangeAcquire(-5, 7));
        assertEquals(-4, ai.get());
        assertEquals(-4, ai.compareAndExchangeAcquire(-4, 7));
        assertEquals(7, ai.get());
    }

    /**
     * compareAndExchangeRelease succeeds in changing value if equal to
     * expected else fails
     */
    public void testCompareAndExchangeRelease() {
        AtomicInteger ai = new AtomicInteger(1);
        assertEquals(1, ai.compareAndExchangeRelease(1, 2));
        assertEquals(2, ai.compareAndExchangeRelease(2, -4));
        assertEquals(-4, ai.get());
        assertEquals(-4, ai.compareAndExchangeRelease(-5, 7));
        assertEquals(-4, ai.get());
        assertEquals(-4, ai.compareAndExchangeRelease(-4, 7));
        assertEquals(7, ai.get());
    }

    /**
     * repeated weakCompareAndSetPlain succeeds in changing value when equal
     * to expected
     */
    public void testWeakCompareAndSetPlain() {
        AtomicInteger ai = new AtomicInteger(1);
        do {} while (!ai.weakCompareAndSetPlain(1, 2));
        do {} while (!ai.weakCompareAndSetPlain(2, -4));
        assertEquals(-4, ai.get());
        do {} while (!ai.weakCompareAndSetPlain(-4, 7));
        assertEquals(7, ai.get());
    }

    /**
     * repeated weakCompareAndSetVolatile succeeds in changing value when equal
     * to expected
     */
    public void testWeakCompareAndSetVolatile() {
        AtomicInteger ai = new AtomicInteger(1);
        do {} while (!ai.weakCompareAndSetVolatile(1, 2));
        do {} while (!ai.weakCompareAndSetVolatile(2, -4));
        assertEquals(-4, ai.get());
        do {} while (!ai.weakCompareAndSetVolatile(-4, 7));
        assertEquals(7, ai.get());
    }

    /**
     * repeated weakCompareAndSetAcquire succeeds in changing value when equal
     * to expected
     */
    public void testWeakCompareAndSetAcquire() {
        AtomicInteger ai = new AtomicInteger(1);
        do {} while (!ai.weakCompareAndSetAcquire(1, 2));
        do {} while (!ai.weakCompareAndSetAcquire(2, -4));
        assertEquals(-4, ai.get());
        do {} while (!ai.weakCompareAndSetAcquire(-4, 7));
        assertEquals(7, ai.get());
    }

    /**
     * repeated weakCompareAndSetRelease succeeds in changing value when equal
     * to expected
     */
    public void testWeakCompareAndSetRelease() {
        AtomicInteger ai = new AtomicInteger(1);
        do {} while (!ai.weakCompareAndSetRelease(1, 2));
        do {} while (!ai.weakCompareAndSetRelease(2, -4));
        assertEquals(-4, ai.get());
        do {} while (!ai.weakCompareAndSetRelease(-4, 7));
        assertEquals(7, ai.get());
    }

}
