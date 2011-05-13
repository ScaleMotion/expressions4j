package jexpr;

/**
 * Exception that indicates that java failed to compile
 * expression
 */
public class ExpressionCompilationException extends RuntimeException {
    public ExpressionCompilationException(String message) {
        super(message);
    }

    public ExpressionCompilationException(String message, Throwable cause) {
        super(message, cause);
    }
}
