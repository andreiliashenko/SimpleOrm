package com.anli.simpleorm.definitions;

public class CollectionDefinition extends ReferenceDefinition {

    public CollectionDefinition(String name, String foreignKeyColumn,
            EntityDefinition referencedEntity, boolean lazy) {
        super(name, foreignKeyColumn, referencedEntity, lazy);
    }

    @Override
    public String getColumn() {
        return referencedEntity.getPrimaryKey().getColumn();
    }

    public String getForeignKeyColumn() {
        return super.getColumn();
    }
}
