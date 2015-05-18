package com.anli.simpleorm.reflective;

import com.anli.simpleorm.lazy.LazyValue;
import java.util.HashMap;
import java.util.Map;
import javassist.util.proxy.ProxyObject;

public abstract class EntityProcessor {

    protected final Class entityClass;
    protected final Class proxyClass;
    protected final Map<String, FieldProcessor> fields;
    protected String primaryKeyName;

    protected final MethodHandlerData handlerData;

    public EntityProcessor(Class entityClass, Class proxyClass, MethodHandlerData handlerData) {
        this.entityClass = entityClass;
        this.proxyClass = proxyClass;
        this.handlerData = handlerData;
        this.fields = new HashMap<>();
    }

    public void addFieldProcessor(FieldProcessor field) {
        fields.put(field.getName(), field);
    }

    public void addPrimaryKeyProcessor(FieldProcessor field) {
        primaryKeyName = field.getName();
        addFieldProcessor(field);
    }

    public void setField(Object entity, String fieldName, Object fieldValue) {
        fields.get(fieldName).set(entity, fieldValue);
    }

    public void setPrimaryKey(Object entity, Object primaryKey) {
        setField(entity, primaryKeyName, primaryKey);
    }

    public void setLazyReference(Object entity, String fieldName, LazyValue lazyValue) {
        if (!isProxied()) {
            return;
        }
        EntityMethodHandler handler = getHandler(entity);
        handler.setLazyReferenceValue(fieldName, lazyValue);
    }

    public Object getLazyKey(Object entity, String fieldName) {
        if (!isProxied()) {
            return null;
        }
        EntityMethodHandler handler = getHandler(entity);
        return handler.getLazyReferenceKey(fieldName);
    }

    public Object getField(Object entity, String fieldName) {
        return fields.get(fieldName).get(entity);
    }

    public boolean isLazyClean(Object entity, String fieldName) {
        if (!isProxied()) {
            return false;
        }
        return getHandler(entity).isLazyClean(fieldName);
    }

    public Object getPrimaryKey(Object entity) {
        return getField(entity, primaryKeyName);
    }

    public <E> E getInstance() {
        Object instance;
        if (isProxied()) {
            instance = instantiate(proxyClass);
            ((ProxyObject) instance).setHandler(getMethodHandler());
        } else {
            instance = instantiate(entityClass);
        }
        return (E) instance;
    }

    protected Object instantiate(Class clazz) {
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }

    protected EntityMethodHandler getHandler(Object entity) {
        ProxyObject proxy = (ProxyObject) entity;
        return (EntityMethodHandler) proxy.getHandler();
    }

    protected boolean isProxied() {
        return proxyClass != null;
    }

    protected MethodHandlerData getHandlerData() {
        return handlerData;
    }

    protected abstract EntityMethodHandler getMethodHandler();
}
