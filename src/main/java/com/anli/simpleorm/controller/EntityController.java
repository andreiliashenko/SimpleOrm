package com.anli.simpleorm.controller;

public interface EntityController {

    <E> E create(Class<E> entityClass);

    <E> E getByPrimaryKey(Object primaryKey, Class<E> entityClass);

    void remove(Object entity);
}
