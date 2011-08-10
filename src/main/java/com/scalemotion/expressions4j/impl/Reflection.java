package com.scalemotion.expressions4j.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Utility methods for working with reflection
 */
public class Reflection {
    static final Map<Class, Class> primitives = new HashMap<Class, Class>();

    static Class wrapType(Class returnType) {
        if (returnType.isPrimitive()) {
            return primitives.get(returnType);
        } else {
            return returnType;
        }
    }

    public static Field field(Class cls, String name) {
        for (Field f : fields(cls)) {
            if (f.getName().equals(name)) {
                return f;
            }
        }
        return null;
    }

    static String className(Class cls) {
        return cls.getCanonicalName();
    }

    static List<Field> fields(Class cls) {
        List<Field> r = new ArrayList<Field>();
        while (cls != null) {
            r.addAll(Arrays.asList(cls.getDeclaredFields()));
            cls = cls.getSuperclass();
        }
        return r;
    }

    static List<Method> methods(Class cls) {
        List<Method> r = new ArrayList<Method>();
        while (cls != null) {
            r.addAll(Arrays.asList(cls.getDeclaredMethods()));
            cls = cls.getSuperclass();
        }
        return r;
    }

    static String getterToName(String name) {
        if (name.startsWith("is")) {
            return firstLetterToLower(name.substring("is".length()));
        } else if (name.startsWith("get")) {
            return firstLetterToLower(name.substring("get".length()));
        } else {
            return null;
        }
    }

    static String firstLetterToLower(String name) {
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }
}
