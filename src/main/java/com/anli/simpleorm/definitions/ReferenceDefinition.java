package com.anli.simpleorm.definitions;

public class ReferenceDefinition extends FieldDefinition {

    protected EntityDefinition referencedEntity;
    protected boolean lazy;

    public ReferenceDefinition(String name, String column,
            EntityDefinition referencedEntity, boolean lazy) {
        super(name, column);
        this.referencedEntity = referencedEntity;
        this.lazy = lazy;
    }

    public EntityDefinition getReferencedEntity() {
        return referencedEntity;
    }

    public boolean isLazy() {
        return lazy;
    }
}
