package com.anli.simpleorm.definitions;

public class PrimitiveDefinition extends FieldDefinition {

    protected final PrimitiveType type;

    public PrimitiveDefinition(String name, Class<?> javaClass, String column, PrimitiveType type) {
        super(name, javaClass, column);
        this.type = type;
    }

    public PrimitiveType getType() {
        return type;
    }
}
