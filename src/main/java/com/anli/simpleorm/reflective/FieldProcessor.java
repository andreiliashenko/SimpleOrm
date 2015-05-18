package com.anli.simpleorm.reflective;

import java.lang.reflect.Field;

public class FieldProcessor {

    protected final Field field;
    protected final String name;

    public FieldProcessor(Field field, String name) {
        this.field = field;
        this.name = name;
        this.field.setAccessible(true);
    }

    public String getName() {
        return name;
    }

    public Object get(Object entity) {
        try {
            return field.get(entity);
        } catch (IllegalAccessException | IllegalArgumentException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void set(Object entity, Object value) {
        try {
            field.set(entity, value);
        } catch (IllegalAccessException | IllegalArgumentException ex) {
            throw new RuntimeException(ex);
        }
    }
}
