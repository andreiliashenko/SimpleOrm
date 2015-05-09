package com.anli.simpleorm.definitions;

public class FieldDefinition {

    protected String name;
    protected String column;

    public FieldDefinition(String name, String column) {
        this.name = name;
        this.column = column;
    }

    public String getName() {
        return name;
    }

    public String getColumn() {
        return column;
    }
}
