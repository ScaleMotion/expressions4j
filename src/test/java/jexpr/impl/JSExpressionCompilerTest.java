package jexpr.impl;

import jexpr.ExpressionCompiler;

public class JSExpressionCompilerTest extends AbstractExpressionCompilerTest {
    @Override
    protected String simpleTestExpression() {
        return "2 * 2";
    }

    @Override
    protected ExpressionCompiler createCompiler() {
        return new JSExpressionCompiler();
    }

    @Override
    protected String mapTestExpression() {
        return "a + b";
    }

    @Override
    protected String constantsAndFunctionsExpression() {
        return "var sq = square(five); var sum = add(a, b); sq + sum";
    }
}
