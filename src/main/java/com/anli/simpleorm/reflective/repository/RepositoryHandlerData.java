package com.anli.simpleorm.reflective.repository;

import com.anli.simpleorm.reflective.FieldProcessor;
import com.anli.simpleorm.reflective.MethodHandlerData;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class RepositoryHandlerData extends MethodHandlerData {

    protected final Map<Method, FieldProcessor> lazyCollectionGetters;
    protected final Map<Method, FieldProcessor> lazyCollectionSetters;

    public RepositoryHandlerData() {
        super();
        this.lazyCollectionGetters = new HashMap<>();
        this.lazyCollectionSetters = new HashMap<>();
    }

    public void addCollectionProcessorForGetter(Method getter, FieldProcessor processor) {
        lazyCollectionGetters.put(getter, processor);
    }

    public void addCollectionProcessorForSetter(Method setter, FieldProcessor processor) {
        lazyCollectionSetters.put(setter, processor);
    }

    public FieldProcessor getLazyCollectionProcessorByGetter(Method getter) {
        return lazyCollectionGetters.get(getter);
    }

    public FieldProcessor getLazyCollectionProcessorBySetter(Method setter) {
        return lazyCollectionSetters.get(setter);
    }
}
