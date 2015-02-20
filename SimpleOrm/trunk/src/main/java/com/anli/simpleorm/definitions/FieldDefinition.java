package com.anli.simpleorm.definitions;

public abstract class FieldDefinition {

    protected String name;
    protected Class<?> javaClass;
    protected String column;

    public FieldDefinition(String name, Class<?> javaClass, String column) {
        this.name = name;
        this.javaClass = javaClass;
        this.column = column;
    }

    public String getName() {
        return name;
    }

    public Class<?> getJavaClass() {
        return javaClass;
    }

    public String getColumn() {
        return column;
    }
}
