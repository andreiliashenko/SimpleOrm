package com.anli.simpleorm.reflective.repository;

import com.anli.simpleorm.reflective.EntityMethodHandler;
import com.anli.simpleorm.reflective.EntityProcessor;

public class RepositoryProcessor extends EntityProcessor {

    public RepositoryProcessor(Class entityClass, Class proxyClass,
            RepositoryHandlerData handlerData) {
        super(entityClass, proxyClass, handlerData);
    }

    public RepositoryProcessor(Class entityClass) {
        this(entityClass, null, null);
    }

    public void markLazyCollection(Object entity, String field) {
        ((RepositoryMethodHandler) getHandler(entity)).setLazyCollectionClean(field);
    }

    @Override
    protected EntityMethodHandler getMethodHandler() {
        return new RepositoryMethodHandler((RepositoryHandlerData) getHandlerData());
    }
}
