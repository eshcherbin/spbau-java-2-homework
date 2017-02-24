import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Supplier;

/**
 * A class that provides three implementations of the Lazy interface.
 */
public class LazyFactory {
    /**
     * Creates a simple lazy evaluation that provides no guarantees when used concurrently.
     * @param supplier the evaluation that is to be done
     * @param <T> the type of the evaluation result
     * @return the lazy evaluation
     */
    @NotNull
    public static <T> Lazy<T> createLazyNonConcurrent(@NotNull Supplier<T> supplier) {
        return new LazyImplNonConcurrent<>(supplier);
    }

    /**
     * Creates a lazy evaluation that uses synchronisation to provide desired effects when used concurrently.
     * @param supplier the evaluation that is to be done
     * @param <T> the type of the evaluation result
     * @return the lazy evaluation
     */
    @NotNull
    public static <T> Lazy<T> createLazyConcurrent(@NotNull Supplier<T> supplier) {
        return new LazyImplConcurrent<>(supplier);
    }

    /**
     * Creates a lazy evaluation that uses lock-free primitives to provide desired effects when used concurrently.
     * Notice that the supplier's `get` method can be called several times.
     * @param supplier the evaluation that is to be done
     * @param <T> the type of the evaluation result
     * @return the lazy evaluation
     */
    @NotNull
    public static <T> Lazy<T> createLazyConcurrentLockFree(@NotNull Supplier<T> supplier) {
        return new LazyImplConcurrentLockFree<>(supplier);
    }


    /**
     * An abstract class for the following Lazy implementations
     * @param <T> the type of the evaluation result
     */
    private static abstract class LazyImpl<T> implements Lazy<T> {
        /**
         * A dummy object representing a lack of result.
         */
        protected static Object noResultYet = new Object();

        /**
         * The evaluation to be done.
         */
        protected Supplier<T> supplier;
        /**
         * The result of the evaluation.
         */
        protected volatile T result = (T) noResultYet;

        private LazyImpl(@NotNull Supplier<T> supplier) {
            this.supplier = supplier;
        }
    }

    /**
     * A simple Lazy implementation that provides no guarantees when used concurrently.
     * @param <T> the type of the evaluation result
     */
    private static class LazyImplNonConcurrent<T> extends LazyImpl<T> {
        private LazyImplNonConcurrent(@NotNull Supplier<T> supplier) {
            super(supplier);
        }

        /**
         * Returns the evaluation result.
         * @return the evaluation result
         */
        @Override
        @Nullable
        public T get() {
            if (result == noResultYet) {
                result = supplier.get();
                supplier = null;
            }
            return result;
        }
    }

    /**
     * A Lazy implementation that uses synchronisation to provide desired effects when used concurrently.
     * @param <T> the type of the evaluation result
     */
    private static class LazyImplConcurrent<T> extends LazyImpl<T> {
        private LazyImplConcurrent(@NotNull Supplier<T> supplier) {
            super(supplier);
        }

        /**
         * Returns the evaluation result.
         * @return the evaluation result
         */
        @Override
        @Nullable
        public T get() {
            if (result != noResultYet) {
                return result;
            }
            synchronized (this) {
                if (result == noResultYet) {
                    result = supplier.get();
                    supplier = null;
                }
            }
            return result;
        }
    }

    /**
     * A Lazy implementation that uses lock-free primitives to provide desired effects when used concurrently.
     * Notice that the supplier's `get` method can be called several times.
     * @param <T> the type of the evaluation result
     */
    private static class LazyImplConcurrentLockFree<T> extends LazyImpl<T> {
        /**
         * An atomic updater of the `result` field.
         */
        private static final AtomicReferenceFieldUpdater<LazyImpl, Object> atomicResultUpdater =
                AtomicReferenceFieldUpdater.newUpdater(LazyImpl.class, Object.class, "result");

        private LazyImplConcurrentLockFree(@NotNull Supplier<T> supplier) {
            super(supplier);
        }

        /**
         * Returns the evaluation result.
         * @return the evaluation result
         */
        @Override
        @Nullable
        public T get() {
            if (result == noResultYet) {
                atomicResultUpdater.compareAndSet(this, noResultYet, supplier.get());
            }
            return result;
        }
    }
}
