import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class LazyFactory {
    @NotNull
    public static <T> Lazy<T> createLazyNonConcurrent(@NotNull Supplier<T> supplier) {
        return new LazyImplNonConcurrent<>(supplier);
    }

    @NotNull
    public static <T> Lazy<T> createLazyConcurrent(@NotNull Supplier<T> supplier) {
        return new LazyImplConcurrent<>(supplier);
    }

    @NotNull
    public static <T> Lazy<T> createLazyConcurrentLockFree(@NotNull Supplier<T> supplier) {
        return null;
    }

    private static abstract class LazyImpl<T> implements Lazy<T> {
        protected static Object noResultYet = new Object();

        protected Supplier<T> supplier;
        protected volatile T result = (T) noResultYet;

        private LazyImpl(@NotNull Supplier<T> supplier) {
            this.supplier = supplier;
        }
    }

    private static class LazyImplNonConcurrent<T> extends LazyImpl<T> {
        private LazyImplNonConcurrent(@NotNull Supplier<T> supplier) {
            super(supplier);
        }

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

    private static class LazyImplConcurrent<T> extends LazyImpl<T> {
        private LazyImplConcurrent(@NotNull Supplier<T> supplier) {
            super(supplier);
        }

        @Override
        @Nullable
        public T get() {
            if (result != noResultYet) {
                return result;
            }
            synchronized (this) {
                if (result != noResultYet) {
                    result = supplier.get();
                    supplier = null;
                }
            }
            return result;
        }
    }
}
