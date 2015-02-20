package com.anli.simpleorm.definitions;

public class ReferenceDefinition extends FieldDefinition {

    protected EntityDefinition referencedEntity;

    public ReferenceDefinition(String name, Class<?> javaClass, String column, EntityDefinition referencedEntity) {
        super(name, javaClass, column);
        this.referencedEntity = referencedEntity;
    }

    public EntityDefinition getReferencedEntity() {
        return referencedEntity;
    }
}
