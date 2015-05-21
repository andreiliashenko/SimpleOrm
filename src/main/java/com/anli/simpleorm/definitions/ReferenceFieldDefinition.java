package com.anli.simpleorm.definitions;

public class ReferenceFieldDefinition extends FieldDefinition {

    protected EntityDefinition referencedDefinition;
    protected boolean lazy;

    public ReferenceFieldDefinition(String name, String column,
            EntityDefinition referencedEntity, boolean lazy) {
        super(name, column);
        this.referencedDefinition = referencedEntity;
        this.lazy = lazy;
    }

    public EntityDefinition getReferencedDefinition() {
        return referencedDefinition;
    }

    public boolean isLazy() {
        return lazy;
    }
}
