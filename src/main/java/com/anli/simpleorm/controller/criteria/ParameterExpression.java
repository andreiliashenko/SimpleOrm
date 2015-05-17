package com.anli.simpleorm.controller.criteria;

public interface ParameterExpression<V> extends Expression<V> {

    Class<? extends V> getBindingClass();
}
