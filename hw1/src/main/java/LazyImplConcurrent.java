import java.util.function.Supplier;

public class LazyImplConcurrent<T> implements Lazy<T> {
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
