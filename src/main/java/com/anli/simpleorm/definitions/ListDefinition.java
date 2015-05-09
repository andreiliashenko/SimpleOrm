package com.anli.simpleorm.definitions;

public class ListDefinition extends CollectionDefinition {

    protected String orderColumn;

    public ListDefinition(String name, String foreignKeyColumn,
            EntityDefinition referencedEntity, String orderColumn, boolean lazy) {
        super(name, foreignKeyColumn, referencedEntity, lazy);
        this.orderColumn = orderColumn;
    }

    public String getOrderColumn() {
        return orderColumn;
    }
}
