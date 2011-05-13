package jexpr;

/**
 * Interface responsible for compiling
 * expressions. It's subclasses implements different language
 */
public interface ExpressionCompiler {
    /**
     * Adds constant value that will be accessible in all expressions compiled with this compiler
     * @param name name of the constant
     * @param type type of the constant
     * @param value value of the constant
     * @param <T> constant value type
     */
    public <T> void addConstant(String name, Class<T> type, T value);

    /**
     * Adds function that will be accessible in all expressions compiled by this compiler
     * @param name name of the function
     * @param parameterTypes list of parameter types
     * @param returnType return type
     * @param function function
     * @param <T> type of return value
     */
    public <T> void addFunction(String name, Class[] parameterTypes, Class<T> returnType, ExpressionFunction<T> function);

    /**
     * Compiles expression
     * @param expressionText expression text
     * @param contextType type of context
     * @param returnType return type
     * @param <K> context class ({@see CompiledExpression})
     * @param <V> return type ({@see CompiledExpression})
     * @return compiled expression
     */
    public <K, V> CompiledExpression<K, V> compile(String expressionText, Class<K> contextType, Class<V> returnType);
}
