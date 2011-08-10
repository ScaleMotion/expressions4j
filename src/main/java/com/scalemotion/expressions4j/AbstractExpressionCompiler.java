package com.scalemotion.expressions4j;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract {@link ExpressionCompiler} implementation that
 * implements some common things
 */
public abstract class AbstractExpressionCompiler implements ExpressionCompiler{
    private List<Constant> constants = new ArrayList<Constant>();
    private List<Function> functions = new ArrayList<Function>();

    @Override
    public <T> void addConstant(String name, Class<T> type, T value) {
        constants.add(new Constant(name, type, value));
    }

    @Override
    public <T> void addFunction(String name, Class[] parameterTypes, Class<T> returnType, ExpressionFunction<T> function) {
        functions.add(new Function(name, parameterTypes, returnType, function));
    }

    protected List<Constant> getConstants() {
        return constants;
    }

    protected List<Function> getFunctions() {
        return functions;
    }

    protected static class Function {
        private String name;
        private Class[] parameterTypes;
        private Class returnType;
        private ExpressionFunction function;

        public Function(String name, Class[] parameterTypes, Class returnType, ExpressionFunction function) {
            this.name = name;
            this.parameterTypes = parameterTypes;
            this.returnType = returnType;
            this.function = function;
        }

        public String getName() {
            return name;
        }

        public Class[] getParameterTypes() {
            return parameterTypes;
        }

        public Class getReturnType() {
            return returnType;
        }

        public ExpressionFunction getFunction() {
            return function;
        }
    }

    protected static class Constant {
        private String name;
        private Class type;
        private Object value;

        public Constant(String name, Class type, Object value) {
            this.name = name;
            this.type = type;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public Class getType() {
            return type;
        }

        public Object getValue() {
            return value;
        }
    }
}
