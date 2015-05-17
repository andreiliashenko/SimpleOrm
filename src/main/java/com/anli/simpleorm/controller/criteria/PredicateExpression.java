package com.anli.simpleorm.controller.criteria;

public interface PredicateExpression<L, R> extends Expression<Boolean> {

    Expression<L> getLeftOperand();

    Expression<R> getRightOperand();
}
