package com.anli.simpleorm.reflective;

public class EntityProcessor {

    public void setPrimaryKey(Object entity, Object primaryKey) {

    }

    public void setField(Object entity, String fieldName, Object fieldValue) {
        setField(entity, fieldName, fieldValue, false);
    }

    public void setField(Object entity, String fieldName, Object fieldValue, boolean lazy) {

    }

    public Object getPrimaryKey(Object entity) {
        return null;
    }

    public Object getField(Object entity, String fieldName) {
        return null;
    }
}
