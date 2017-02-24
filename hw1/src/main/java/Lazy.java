/**
 * An interface representing a lazy evaluation.
 * The evaluation is done only when it's first needed (i.e. during the first `get` call).
 * The subsequent `get` calls should return the same object as the first one.
 * @param <T> The type of the evaluation result
 */
public interface Lazy<T> {
    /**
     * Returns the evaluation result.
     * @return the evaluation result
     */
    T get();
}
