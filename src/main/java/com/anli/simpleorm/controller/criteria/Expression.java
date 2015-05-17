package com.anli.simpleorm.controller.criteria;

public interface Expression<V> {

    V evaluate(Object operand);
}
