package jexpr;

/**
 * Function that could be injected into expressions compiler
 * @param <T> function return type
 */
public interface ExpressionFunction<T> {
    /**
     * Calls the function
     * @param o parameters
     * @return return value
     */
    public T call(Object ...o);
}
