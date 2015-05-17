package com.anli.simpleorm.reflective;

public class EntityProcessor {

    public void setPrimaryKey(Object entity, Object primaryKey) {

    }

    public void setField(Object entity, String fieldName, Object fieldValue) {
    }

    public void setLazyKey(Object entity, String fieldName, Object key) {

    }

    public Object getLazyKey(Object entity, String fieldName) {
        return null;
    }

    public boolean isLazyClean(Object entity, String fieldName) {
        return false;
    }

    public Object getPrimaryKey(Object entity) {
        return null;
    }

    public Object getField(Object entity, String fieldName) {
        return null;
    }

    public <E> E getInstance() {
        return null;
    }
}
