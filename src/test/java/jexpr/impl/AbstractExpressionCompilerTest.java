package jexpr.impl;

import jexpr.CompiledExpression;
import jexpr.ExpressionCompiler;
import jexpr.ExpressionFunction;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractExpressionCompilerTest {
    @Test
    public void testSimple() {
        CompiledExpression<Void, Integer> compiled = createCompiler().compile(simpleTestExpression(), Void.class, int.class);
        Assert.assertEquals(Integer.valueOf(4), compiled.evaluate(null));
    }

    protected abstract String simpleTestExpression();

    @Test
    public void testWithMapContext() {
        //Comments in testWithConstantsAndFunctions() explains this weird typecast
        CompiledExpression<Map, Number> compiled = (CompiledExpression) createCompiler().compile(mapTestExpression(), Map.class, Integer.class);
        Map<String, Integer> m = new HashMap<String, Integer>();
        m.put("a", 1);
        m.put("b", 2);
        Number res = compiled.evaluate(m);
        Assert.assertEquals(3, res.intValue());
    }

    protected abstract ExpressionCompiler createCompiler();

    protected abstract String mapTestExpression();

    @Test
    public void testWithConstantsAndFunctions() {
        ExpressionCompiler compiler = createCompiler();
        compiler.addFunction("square", new Class[]{int.class}, int.class, new ExpressionFunction<Integer>() {
            @Override
            public Integer call(Object... o) {
                int i = (Integer)o[0];
                return i*i;
            }
        });
        compiler.addFunction("add", new Class[]{int.class, int.class}, int.class, new ExpressionFunction<Integer>() {
            @Override
            public Integer call(Object... o) {
                return (Integer)o[0] + (Integer)o[1];
            }
        });
        compiler.addConstant("five", int.class, 5);
        //This weird typecast and Number return type is here to keep test code generic for Java and JS engine.
        //JavaScript sometimes return Double as a result of operation with integers, and some times it's Integer
        //(it's according to standard though, JS has only a type called Number that is user for ints and floats both)
        CompiledExpression<JavaExpressionCompilerTest.Context, Number> compiled =
                (CompiledExpression) compiler.compile(constantsAndFunctionsExpression(), JavaExpressionCompilerTest.Context.class, int.class);
        Number res = compiled.evaluate(new JavaExpressionCompilerTest.Context());
        Assert.assertEquals(40, res.intValue());
    }

    protected abstract String constantsAndFunctionsExpression();
}
