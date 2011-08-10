package com.scalemotion.expressions4j.impl;

import javassist.*;
import javassist.Modifier;
import com.scalemotion.expressions4j.AbstractExpressionCompiler;
import com.scalemotion.expressions4j.CompiledExpression;
import com.scalemotion.expressions4j.ExpressionCompilationException;
import com.scalemotion.expressions4j.ExpressionFunction;

import java.lang.reflect.*;
import java.util.*;

import static java.lang.String.format;
import static java.lang.reflect.Modifier.*;

public class JavaExpressionCompiler extends AbstractExpressionCompiler {

    static {
        Reflection.primitives.put(byte.class, Byte.class);
        Reflection.primitives.put(short.class, Short.class);
        Reflection.primitives.put(int.class, Integer.class);
        Reflection.primitives.put(long.class, Long.class);
        Reflection.primitives.put(float.class, Float.class);
        Reflection.primitives.put(double.class, Double.class);
        Reflection.primitives.put(char.class, Character.class);
        Reflection.primitives.put(boolean.class, Boolean.class);
    }

    private List<String> importedPackages = new ArrayList<String>();

    /**
     * Imports all classes from given packages. Classes inside expression would
     * be availale simply by name.
     *
     * @param packageName package name
     */
    public void addPackageImport(String packageName) {
        importedPackages.add(packageName);
    }

    @Override
    public <K, V> CompiledExpression<K, V> compile(String expressionText, Class<K> contextType, Class<V> returnType) {
        try {
            return compileInternal(expressionText, contextType, returnType);
        } catch (Exception e) {
            throw new ExpressionCompilationException(e.getMessage(), e);
        }
    }

    private <K, V> CompiledExpression<K, V> compileInternal(String expressionText, Class<K> contextType, Class<V> returnType) throws Exception {
        if (contextType == null) {
            contextType = (Class<K>) Object.class;
        }
        if (Map.class.isAssignableFrom(contextType)) {
            throw new IllegalStateException("Java expressions don't suport Map as a context class");
        }
        ClassPool pool = ClassPool.getDefault();
        for (String p : importedPackages) {
            pool.importPackage(p);
        }
        CtClass clsExpression = pool.makeClass("SimpleCompiledExpression$" + UUID.randomUUID());
        clsExpression.addInterface(pool.get(CompiledExpression.class.getName()));
        clsExpression.addConstructor(CtNewConstructor.defaultConstructor(clsExpression));
        //define constant
        for (Constant c : getConstants()) {
            Class constantClass = c.getType();
            CtField field = new CtField(pool.get(constantClass.getName()), c.getName(), clsExpression);
            field.setModifiers(Modifier.setPublic(0));
            clsExpression.addField(field);
        }
        //define functions
        for (Function f : getFunctions()) {
            CtField field = new CtField(pool.get(ExpressionFunction.class.getName()), functionName(f.getName()), clsExpression);
            field.setModifiers(Modifier.setPublic(0));
            clsExpression.addField(field);
            String functionBody = format("public %s %s(%s){\n%s\n}", Reflection.className(f.getReturnType()), f.getName(), functionSignature(f.getParameterTypes()), functionBody(f));
            addMethod(clsExpression, functionBody);
        }
        //generate evaluate method body
        String intMethodBody = format("public %s $$evaluateInternal$$(%s $$ctx$$){\n", Reflection.className(returnType), Reflection.className(contextType));
        for (Field f : Reflection.fields(contextType)) {
            if (isPublic(f.getModifiers()) && !isStatic(f.getModifiers())) {
                intMethodBody += format("%s %s = $$ctx$$.%s;\n", Reflection.className(f.getType()), f.getName(), f.getName());
            }
        }

        for (Method m : Reflection.methods(contextType)) {
            if (isPublic(m.getModifiers()) && !isStatic(m.getModifiers()) && m.getParameterTypes().length == 0 && m.getReturnType() != null && Reflection.getterToName(m.getName()) != null && !"class".equals(Reflection.getterToName(m.getName()))) {
                intMethodBody += format("%s %s = $$ctx$$.%s();\n", Reflection.className(m.getReturnType()), Reflection.getterToName(m.getName()), m.getName());
            }
        }
        intMethodBody += expressionText + ";\n}";
        addMethod(clsExpression, intMethodBody);
        String mainMethodBody = "public Object evaluate(Object $$ctx$$){\n";
        if (returnType == void.class) {
            mainMethodBody += format("$$evaluateInternal$$((%s)$$ctx$$); return null;\n}", Reflection.className(contextType));
        } else {
            mainMethodBody += format("%s $ = $$evaluateInternal$$((%s)$$ctx$$); return %s;\n}", Reflection.className(returnType), Reflection.className(contextType), boxExpr(returnType, "$"));
        }
        addMethod(clsExpression, mainMethodBody);
        Class generatedClass = clsExpression.toClass();
        CompiledExpression<K, V> compiled = (CompiledExpression<K, V>) generatedClass.newInstance();
        for (Constant c : getConstants()) {
            Reflection.field(generatedClass, c.getName()).set(compiled, c.getValue());
        }
        for (Function f : getFunctions()) {
            Reflection.field(generatedClass, functionName(f.getName())).set(compiled, f.getFunction());
        }
        return compiled;

    }


    private void addMethod(CtClass clsExpression, String functionBody) {
        try {
            CtMethod method = CtMethod.make(functionBody, clsExpression);
            clsExpression.addMethod(method);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new ExpressionCompilationException("Can't compile: " + functionBody, e);
        }
    }


    private static String functionBody(Function f) {
        String returnType = Reflection.className(Reflection.wrapType(f.getReturnType()));
        return format("%s $ = (%s) %s.call(new Object[]{%s}); return %s;", returnType, returnType, functionName(f.getName()), functionCallArguments(f), unboxExpr(f.getReturnType(), "$"));
    }

    private static String functionCallArguments(Function f) {
        String body = "";
        for (int i = 0, len = f.getParameterTypes().length; i < len; i++) {
            Class cls = f.getParameterTypes()[i];
            String rawVar = "(" + Reflection.className(Reflection.wrapType(cls)) + ") " + boxExpr(cls, "p" + i);
            body += rawVar;
            if (i != len - 1) {
                body += ", ";
            }
        }
        return body;
    }

    private static String unboxExpr(Class cls, String expr) {
        if (cls.isPrimitive()) {
            return "(" + expr + ")." + cls.getSimpleName() + "Value()";
        } else {
            return expr;
        }
    }

    private static String boxExpr(Class cls, String expr) {
        if (!cls.isPrimitive() || cls.isArray()) {
            return expr;
        } else {
            return Reflection.primitives.get(cls).getName() + ".valueOf(" + expr + ")";
        }
    }

    private static String functionSignature(Class[] params) {
        String signature = "";
        for (int i = 0, getLength = params.length; i < getLength; i++) {
            Class cls = params[i];
            signature += cls.getCanonicalName() + " p" + i;
            if (i != getLength - 1) {
                signature += ", ";
            }
        }
        return signature;
    }

    private static String functionName(String name) {
        return "$$function$$" + name;
    }

}
