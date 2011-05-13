package jexpr.examples;

import jexpr.ExpressionFunction;
import jexpr.impl.JSExpressionCompiler;
import jexpr.impl.JavaExpressionCompiler;

import java.util.HashMap;
import java.util.Map;

/**
 * Class that describes API of jexpr as a set of examples.
 * See https://github.com/vklimontovich/jexpr/blob/master/README.mediawiki
 */
public class Examples {

    public static void main(String[] args) {
        //Calling all demo methods one by one
        simpleJavaExpression();
        contextObject();
        constants();
        functions();
        mapContext();
    }

    public static void simpleJavaExpression() {
        //This example demonstrates how to evaluate simple expression
        Integer javaRes = new JavaExpressionCompiler().compile(
                "return 2 * 2;", // That's an expression text
                null,    // Here you should specify a context class
                //all public fields and properties (indicated by getters)
                //will be available inside expression. That's called and expression context.
                //As here we don't rely on any variables we just put a null
                int.class
        ).evaluate(null);//Context object instance should be passed here. All fields/properties
        //of this objects will be available in expression. The class of context object
        // was specified in constructor
        System.out.println(javaRes); //will output 4. The int result is automatically "boxed" to java.lang.Integer

        //The same API for JavaScript except two things:
        // - We don't need to use return statement in expression (the result is a value of latest expression)
        // - We created different instance of ExpressionCompiler
        Integer jsRes = new JSExpressionCompiler().compile(
                "2 * 2", // That's an expression text
                null,
                int.class
        ).evaluate(null);//Context object instance should be passed here. All fields/properties
        System.out.println(jsRes); //will output 4
    }

    public static void contextObject() {
        //This example demonstrates how to expose variables
        Integer javaRes = new JavaExpressionCompiler().compile(
                "return a * b;",
                ContextObject.class, //here we specify context object class
                //all public fields or properties could be used inside expression (it's a and b in this case)
                int.class
        ).evaluate(new ContextObject(2, 3)); //giving context object instance (a=2, b=3)
        System.out.println(javaRes); //will output 6

        //The same API for JavaScript!
        //The return type is Number because despite the fact only ints participate in expression the result
        //is double. That's a tricky thing about JS. It doesn't distinguish ints/doubles. It has a common
        //Number type. The result could be Integer or Double depending on how rhino optimized the code
        Number jsRes = new JSExpressionCompiler().compile(
                "a * b;",
                ContextObject.class,
                int.class
        ).evaluate(new ContextObject(2, 3)); //giving context object instance (a=2, b=3)
        System.out.println(jsRes); //will output 6.0
    }

    public static void constants() {
        //This example demonstrates how to expose constant to expression
        //The difference with context variable is that you specify
        //the constant once in order not to clutter your context object.
        //The constant will be available in all expressions compiled by this
        //compiler
        JavaExpressionCompiler javaCompiler = new JavaExpressionCompiler();
        javaCompiler.addConstant("TWO", int.class, 2);
        Integer javaRes = javaCompiler.compile(
                "return TWO * TWO;",
                null, //no context object
                int.class
        ).evaluate(null); //giving context object instance (a=2, b=3)
        System.out.println(javaRes); //will output 4
    }

    public static void functions() {
        //This example demonstrates how to expose custom function to expression
        //Here we're using JS compiler. But exactly the same API could be applied
        //to java
        JSExpressionCompiler jsCompiler = new JSExpressionCompiler();
        jsCompiler.addFunction("multiply",
                new Class[]{int.class, int.class} /* parameter types */, int.class /* return type */, new ExpressionFunction<Integer>() {
                    @Override
                    public Integer call(Object... o) {
                        //Parameters were automatically boxed from ints to java.lang.Integers
                        Integer i1 = (Integer) o[1];
                        Integer i2 = (Integer) o[1];
                        return i1 * i2; // the result will be automatically unboxed to int
                    }
                });
        Integer jsRes = jsCompiler.compile(
                "multiply(2, 2);",
                null, //no context object
                int.class
        ).evaluate(null); //giving context object instance (a=2, b=3)
        System.out.println(jsRes); //will output 4
    }
    public static void mapContext() {
        //For javascript Map could be used as context object. All entries of the map
        //would be available as variables in expression.
        //The same won't work in java due to its static typing limitations
        JSExpressionCompiler jsCompiler = new JSExpressionCompiler();
        Map<String, Integer> m = new HashMap<String, Integer>();
        m.put("a", 2);
        m.put("b", 10);
        Number jsRes = jsCompiler.compile(
                "a + b;",
                Map.class, //no context object
                int.class
        ).evaluate(m); //giving context object instance (a=2, b=3)
        System.out.println(jsRes); //will output 12.0
    }


    public static class ContextObject {
        public ContextObject(int a, int b) {
            this.a = a;
            this.b = b;
        }

        public int a;
        private int b;
        public int getB() {
            return b;
        }
    }
}
