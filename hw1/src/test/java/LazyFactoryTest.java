import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.junit.Assert.*;

public class LazyFactoryTest {
    private static final long SUPPLIER_SLEEPING_TIME = 500;
    private static final String TEST_STRING = "testString";
    private static final int THREADS_NUMBER = 10;

    private static int instantSupplierCallsNumber = 0;
    private static int sleepingSupplierCallsNumber = 0;

    private static final Supplier<Object> nullSupplier = () -> null;
    private static final Supplier<String> instantSupplier = () -> {
        ++instantSupplierCallsNumber;
        return TEST_STRING;
    };
    private static final Supplier<String> sleepingSupplier = () -> {
        try {
            Thread.sleep(SUPPLIER_SLEEPING_TIME);
        } catch (InterruptedException e) {
        }
        ++sleepingSupplierCallsNumber;
        return TEST_STRING;
    };

    @Before
    public void setUp() throws Exception {
        instantSupplierCallsNumber = 0;
        sleepingSupplierCallsNumber = 0;
    }

    @Test
    public void testLazyImplNonConcurrent() throws Exception {
        Lazy<String> delayedStringLazy = LazyFactory.createLazyNonConcurrent(sleepingSupplier);
        String result1 = delayedStringLazy.get();
        assertEquals(result1, TEST_STRING);
        String result2 = delayedStringLazy.get();
        assertSame(result1, result2);
        assertEquals(1, sleepingSupplierCallsNumber);
    }

    @Test
    public void testLazyImplNonConcurrentWhenSupplierIsUsedSeveralTimes() throws Exception {
        Lazy<String> instantStringLazy = LazyFactory.createLazyNonConcurrent(instantSupplier);
        String result1 = instantStringLazy.get();
        assertEquals(result1, TEST_STRING);
        String result2 = instantStringLazy.get();
        assertSame(result1, result2);
        assertEquals(1, instantSupplierCallsNumber);

        Lazy<String> anotherInstantStringLazy = LazyFactory.createLazyNonConcurrent(instantSupplier);
        String anotherResult1 = anotherInstantStringLazy.get();
        assertEquals(anotherResult1, TEST_STRING);
        String anotherResult2 = anotherInstantStringLazy.get();
        assertSame(anotherResult1, anotherResult2);
        assertEquals(2, instantSupplierCallsNumber);
    }

    @Test
    public void testLazyImplNonConcurrentNullSupplier() throws Exception {
        Lazy<Object> nullLazy = LazyFactory.createLazyNonConcurrent(nullSupplier);
        Object result = nullLazy.get();
        assertNull(result);
    }

    @Test
    public void testLazyImplNonConcurrentNoSupplierCalled() throws Exception {
        Lazy<String> instantStringLazy = LazyFactory.createLazyNonConcurrent(instantSupplier);
        assertEquals(0, instantSupplierCallsNumber);
    }

    @Test
    public void testLazyImplConcurrent() throws Exception {
        Lazy<String> delayedStringLazy = LazyFactory.createLazyConcurrent(sleepingSupplier);
        String[] results = new String[THREADS_NUMBER];
        Thread[] threads = new Thread[THREADS_NUMBER];
        for (int i = 0; i < threads.length; ++i) {
            int threadIndex = i; // cannot just capture `i` -- need an effectively final variable
            threads[i] = new Thread(() -> results[threadIndex] = delayedStringLazy.get());
        }
        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
        assertEquals(1, sleepingSupplierCallsNumber);
        assertEquals(TEST_STRING, results[0]);
        for (int i = 1; i < results.length; ++i) {
            assertSame(results[0], results[i]);
        }
    }

    @Test
    public void testLazyImplConcurrentLockFree() throws Exception {
        Lazy<String> delayedStringLazy = LazyFactory.createLazyConcurrentLockFree(sleepingSupplier);
        String[] results = new String[THREADS_NUMBER];
        Thread[] threads = new Thread[THREADS_NUMBER];
        for (int i = 0; i < threads.length; ++i) {
            int threadIndex = i; // cannot just capture `i` -- need an effectively final variable
            threads[i] = new Thread(() -> results[threadIndex] = delayedStringLazy.get());
        }
        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
        System.out.println(sleepingSupplierCallsNumber);
        assertEquals(TEST_STRING, results[0]);
        for (int i = 1; i < results.length; ++i) {
            assertSame(results[0], results[i]);
        }
    }
}