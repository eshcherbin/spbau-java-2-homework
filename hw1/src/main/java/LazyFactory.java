import java.util.function.Supplier;

public class LazyFactory {
    public static <T> Lazy<T> createLazyNonConcurrent(Supplier<T> supplier) {
        return null;
    }

    public static <T> Lazy<T> createLazyConcurrent(Supplier<T> supplier) {
        return new LazyImplConcurrent<>(supplier);
    }

    public static <T> Lazy<T> createLazyConcurrentLockFree(Supplier<T> supplier) {
        return null;
    }

    private static class LazyImplConcurrent<T> implements Lazy<T> {
        private static Object noResultYet = new Object();

        private Supplier<T> supplier;
        private volatile T result = (T) noResultYet;

        private LazyImplConcurrent(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        @Override
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
