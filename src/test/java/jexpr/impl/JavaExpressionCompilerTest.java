package jexpr.impl;

import jexpr.ExpressionCompilationException;
import jexpr.ExpressionCompiler;
import org.junit.Test;

public class JavaExpressionCompilerTest extends AbstractExpressionCompilerTest {

    @Override
    protected String simpleTestExpression() {
        return "return 2 * 2";
    }

    @Override
    protected String mapTestExpression() {
        return "return a + b";
    }

    @Override
    protected String constantsAndFunctionsExpression() {
        return "return square(five) + add(a, b)";
    }

    @Override
    @Test(expected = ExpressionCompilationException.class)
    public void testWithMapContext() {
        super.testWithMapContext();
    }

    @Override
    protected ExpressionCompiler createCompiler() {
        return new JavaExpressionCompiler();
    }

    public static class Context {
        public int a = 7;
        public int getB() {
            return 8;
        }
    }
}
