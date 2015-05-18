package com.anli.simpleorm.reflective;

import com.anli.simpleorm.lazy.LazyValue;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import javassist.util.proxy.MethodHandler;

public abstract class EntityMethodHandler implements MethodHandler {

    protected final MethodHandlerData handlerData;
    protected final Map<String, LazyValue> lazyReferences;

    public EntityMethodHandler(MethodHandlerData handlerData) {
        this.handlerData = handlerData;
        this.lazyReferences = new HashMap<>();
    }

    public void setLazyReferenceValue(String field, LazyValue lazyValue) {
        lazyReferences.put(field, lazyValue);
    }

    public Object getLazyReferenceKey(String field) {
        LazyValue lazy = lazyReferences.get(field);
        return lazy != null ? lazy.getKey() : null;
    }

    public void clearLazyReferenceValue(String field) {
        lazyReferences.remove(field);
    }

    public boolean isLazyClean(String field) {
        return lazyReferences.containsKey(field);
    }

    protected MethodHandlerData getHanlderData() {
        return handlerData;
    }

    @Override
    public Object invoke(Object self, Method method, Method proceed, Object[] arguments) throws Throwable {
        FieldProcessor fieldProcessor = getHanlderData().getLazyReferenceProcessorByGetter(proceed);
        if (fieldProcessor != null) {
            String fieldName = fieldProcessor.getName();
            if (isLazyClean(fieldName)) {
                Object value = lazyReferences.get(fieldName).getValue();
                fieldProcessor.set(self, value);
                clearLazyReferenceValue(fieldName);
            }
        }
        Object result = proceed.invoke(self, arguments);
        fieldProcessor = getHanlderData().getLazyReferenceProcessorBySetter(proceed);
        if (fieldProcessor != null) {
            clearLazyReferenceValue(fieldProcessor.getName());
        }
        return result;
    }
}
