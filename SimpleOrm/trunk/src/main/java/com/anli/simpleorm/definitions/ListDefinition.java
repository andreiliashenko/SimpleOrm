package com.anli.simpleorm.definitions;

public class ListDefinition extends CollectionDefinition {

    protected String orderColumn;

    public ListDefinition(String name, Class<?> javaClass, String foreignKeyColumn, EntityDefinition referencedEntity, String orderColumn) {
        super(name, javaClass, foreignKeyColumn, referencedEntity);
        this.orderColumn = orderColumn;
    }

    public String getOrderColumn() {
        return orderColumn;
    }
}
