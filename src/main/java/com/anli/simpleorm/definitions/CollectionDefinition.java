package com.anli.simpleorm.definitions;

public class CollectionDefinition extends ReferenceDefinition {

    public CollectionDefinition(String name, Class<?> javaClass, String foreignKeyColumn,
            EntityDefinition referencedEntity) {
        super(name, javaClass, foreignKeyColumn, referencedEntity);
    }

    @Override
    public String getColumn() {
        return referencedEntity.getPrimaryKey().getColumn();
    }

    public String getForeignKeyColumn() {
        return super.getColumn();
    }
}
