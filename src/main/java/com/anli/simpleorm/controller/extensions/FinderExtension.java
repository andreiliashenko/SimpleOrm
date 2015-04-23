package com.anli.simpleorm.controller.extensions;

import java.util.Collection;
import java.util.List;

public interface FinderExtension {

    <E> List<E> getAll(Class<E> entityClass);

    <E> List<E> getByEquals(String field, Object value, Class<E> entityClass);

    <E> List<E> getByContains(String collectionField, Object value, Class<E> entityClass);

    <E> List<E> getByAny(String field, Collection values, Class<E> entityClass);

    <E> List<E> getByGreater(String field, Object value, boolean strict, Class<E> entityClass);

    <E> List<E> getByLess(String field, Object value, boolean strict, Class<E> entityClass);

    <E> List<E> getByRange(String field, Object left, boolean strictLeft,
            Object right, boolean strictRight, Class<E> entityClass);

    <E> List<E> getByRegexp(String field, String regexp, Class<E> entityClass);

    <E> List<E> getByMacroQuery(String queryName, Collection parameters, Class<E> entityClass);
}
