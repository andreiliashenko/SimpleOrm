package com.anli.simpleorm.reflective.repository;

import com.anli.simpleorm.exceptions.LazyFieldException;
import com.anli.simpleorm.reflective.EntityMethodHandler;
import com.anli.simpleorm.reflective.FieldProcessor;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class RepositoryMethodHandler extends EntityMethodHandler {

    protected final Set<String> cleanLazyCollections;

    public RepositoryMethodHandler(RepositoryHandlerData handlerData) {
        super(handlerData);
        this.cleanLazyCollections = new HashSet<>();
    }

    public void setLazyCollectionClean(String field) {
        cleanLazyCollections.add(field);
    }

    public void clearLazyCollectionClean(String field) {
        cleanLazyCollections.remove(field);
    }

    @Override
    public boolean isLazyClean(String field) {
        if (super.isLazyClean(field)) {
            return true;
        }
        return cleanLazyCollections.contains(field);
    }

    protected RepositoryHandlerData getRepositoryHandlerData() {
        return (RepositoryHandlerData) getHanlderData();
    }

    @Override
    public Object invoke(Object self, Method method, Method proceed, Object[] arguments) throws Throwable {
        FieldProcessor processor = getRepositoryHandlerData()
                .getLazyCollectionProcessorByGetter(method);
        if (processor != null) {
            if (isLazyClean(processor.getName())) {
                throw new LazyFieldException();
            }
        }
        processor = getRepositoryHandlerData()
                .getLazyCollectionProcessorBySetter(method);
        if (processor != null) {
            Object result = proceed.invoke(self, arguments);
            clearLazyCollectionClean(processor.getName());
            return result;
        }
        return super.invoke(self, method, proceed, arguments);
    }
}
