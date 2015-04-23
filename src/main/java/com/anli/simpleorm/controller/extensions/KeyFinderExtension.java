package com.anli.simpleorm.controller.extensions;

import java.util.Collection;
import java.util.List;

public interface KeyFinderExtension {

    <E> List getKeysAll(Class<E> entityClass);

    <E> List getKeysByEquals(String field, Object value, Class<E> entityClass);

    <E> List getKeysByContains(String collectionField, Object value, Class<E> entityClass);

    <E> List getKeysByAny(String field, Collection values, Class<E> entityClass);

    <E> List getKeysByGreater(String field, Object value, boolean strict, Class<E> entityClass);

    <E> List getKeysByLess(String field, Object value, boolean strict, Class<E> entityClass);

    <E> List getKeysByRange(String field, Object left, boolean strictLeft,
            Object right, boolean strictRight, Class<E> entityClass);

    <E> List getKeysByRegexp(String field, String regexp, Class<E> entityClass);

    <E> List getKeysByMacroQuery(String queryName, Collection parameters, Class<E> entityClass);
}
