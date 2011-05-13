package jexpr.impl;

import jexpr.AbstractExpressionCompiler;
import jexpr.CompiledExpression;
import jexpr.ExpressionCompilationException;
import org.mozilla.javascript.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public class JSExpressionEvaluator extends AbstractExpressionCompiler {
    @Override
    public <K, V> CompiledExpression<K, V> compile(String expressionText, Class<K> contextType, Class<V> returnType) {
        final ContextFactory contextFactory = new ContextFactory();
        final Context jsCtx = contextFactory.enterContext();
        jsCtx.setWrapFactory(new JavaObjectWrapper());
        final Script script = jsCtx.compileString(expressionText, "expression.js", -1, null);
        final ScriptableObject prototype = jsCtx.initStandardObjects();
        for (Constant c : getConstants()) {
            prototype.putConst(c.getName(), prototype, c.getValue());
        }
        for (final Function f : getFunctions()) {
            prototype.put(f.getName(), prototype, new BaseFunction() {
                @Override
                public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
                    return f.getFunction().call(args);
                }
            });
        }
        final ScopeVariablesFactory scopeVariablesFactory = new ScopeVariablesFactory(contextType);
        return new CompiledExpression<K, V>() {
            @Override
            public V evaluate(K context) {
                final Context jsCtx = new ContextFactory().enterContext();
                jsCtx.setWrapFactory(new WrapFactory());
                ScriptableObject scope = jsCtx.initStandardObjects();
                scope.setPrototype(prototype);
                scopeVariablesFactory.fillScope(context, scope);
                return (V) script.exec(jsCtx, scope);
            }

            @Override
            public void destruct() {

            }
        };
    }

    private static class ScopeVariablesFactory {
        private Class contextClass;
        private List<Field> contextFields = new ArrayList<Field>();
        private List<Method> contextGetters = new ArrayList<Method>();
        private boolean isMap;

        private ScopeVariablesFactory(Class contextClass) {
            this.contextClass = contextClass;
            if (Map.class.isAssignableFrom(contextClass)) {
                isMap = true;
            } else {
                for (Field f : Reflection.fields(contextClass)) {
                    if (!Modifier.isStatic(f.getModifiers()) && Modifier.isPublic(f.getModifiers()))
                        f.setAccessible(true);
                    contextFields.add(f);
                }
                for (Method f : Reflection.methods(contextClass)) {
                    if (!Modifier.isStatic(f.getModifiers()) && Modifier.isPublic(f.getModifiers()) && Reflection.getterToName(f.getName()) != null && !"getClass".equals(f.getName())) {
                        f.setAccessible(true);
                        contextGetters.add(f);
                    }
                }
            }
        }

        public void fillScope(Object context, ScriptableObject scope) {
            if (isMap) {
                Map map = (Map) context;
                for (Map.Entry e : (Set<Map.Entry>) map.keySet()) {
                    if (e.getKey() == null) {
                        throw new IllegalStateException("Null value couldn't be user as scope variable name");
                    }
                    scope.putConst(e.getKey().toString(), scope, e.getValue());
                }
            } else {
                for (Field f : contextFields) {
                    try {
                        scope.put(f.getName(), scope, f.get(context));
                    } catch (IllegalAccessException e) {
                        throw new ExpressionCompilationException(e.getMessage(), e);
                    }
                }
                for (Method m : contextGetters) {
                    try {
                        scope.put(Reflection.getterToName(m.getName()), scope, m.invoke(context));
                    } catch (Exception e) {
                        throw new ExpressionCompilationException(e.getMessage(), e);
                    }
                }
            }
        }
    }

    private class JavaObjectWrapper extends WrapFactory {
        @Override
        public Scriptable wrapAsJavaObject(Context cx, Scriptable scope, final Object javaObject, Class staticType) {
            Scriptable nativeJava;
            if (javaObject == null) {
                nativeJava = super.wrapAsJavaObject(cx, scope, javaObject, staticType);
            } else if (javaObject instanceof Collection) {
                final Object[] array = ((Collection) javaObject).toArray();
                nativeJava = NativeJavaArray.wrap(scope, array);
            } else if (javaObject instanceof Map) {
                final Map map = (Map) javaObject;
                nativeJava = new NativeObject();
                for (Map.Entry e : (Set<Map.Entry>) map.entrySet()) {
                    nativeJava.put(String.valueOf(e.getKey()), nativeJava, e.getValue() == null ? null : wrap(cx, scope, e.getValue(), e.getValue().getClass()));
                }
            } else {
                nativeJava = super.wrapAsJavaObject(cx, scope, javaObject, staticType);
            }
            final NativeObject prototype = new NativeObject();
            nativeJava.setPrototype(prototype);
            return nativeJava;
        }
    }

}
