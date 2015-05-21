package com.anli.simpleorm.definitions;

public class CollectionFieldDefinition extends ReferenceFieldDefinition {

    public CollectionFieldDefinition(String name, String foreignKeyColumn,
            EntityDefinition referencedEntity, boolean lazy) {
        super(name, foreignKeyColumn, referencedEntity, lazy);
    }

    @Override
    public String getColumn() {
        return referencedDefinition.getPrimaryKey().getColumn();
    }

    public String getForeignKeyColumn() {
        return super.getColumn();
    }
}
