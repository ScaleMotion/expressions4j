package com.scalemotion.expressions4j;

/**
 * Interface that represents compiled expression
 * @param <K> expression return type
 * @param <V> expression context type. If V is an ordinary class all it's fields
 * will be accessible in expression (all fields should be public or have getters). If V is map
 * all it's values will be accessible (in some implementations like java it could
 * be a limitation - map couldn't contain null values)
 */
public interface CompiledExpression<K, V> {
    /**
     * Evaluates expression
     * @param context object
     * @return a value
     */
    public V evaluate(K context);

    /**
     * Compiled expression could keep some resources inside. Despite
     * the fact that java have GC, destruct might be called as Java won't
     * free some type of resources as generated classes
     */
    public void destruct();
}
