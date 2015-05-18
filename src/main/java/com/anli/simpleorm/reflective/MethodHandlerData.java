package com.anli.simpleorm.reflective;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class MethodHandlerData {

    protected final Map<Method, FieldProcessor> lazyReferenceGetters;
    protected final Map<Method, FieldProcessor> lazyReferenceSetters;

    public MethodHandlerData() {
        this.lazyReferenceGetters = new HashMap<>();
        this.lazyReferenceSetters = new HashMap<>();
    }

    public void addReferenceProcessorForGetter(Method getter, FieldProcessor processor) {
        lazyReferenceGetters.put(getter, processor);
    }

    public void addReferenceProcessorForSetter(Method setter, FieldProcessor processor) {
        lazyReferenceSetters.put(setter, processor);
    }

    public FieldProcessor getLazyReferenceProcessorByGetter(Method getter) {
        return lazyReferenceGetters.get(getter);
    }

    public FieldProcessor getLazyReferenceProcessorBySetter(Method setter) {
        return lazyReferenceSetters.get(setter);
    }
}
