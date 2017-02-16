import java.util.function.Supplier;

public class LazyFactory {
    public static <T> Lazy<T> createLazyNonConcurrent(Supplier<T> supplier) {
        return null;
    }

    public static <T> Lazy<T> createLazyConcurrent(Supplier<T> supplier) {
        return null;
    }

    public static <T> Lazy<T> createLazyConcurrentLockFree(Supplier<T> supplier) {
        return null;
    }
}
