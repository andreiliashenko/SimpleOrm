package com.anli.simpleorm.definitions;

public class ListFieldDefinition extends CollectionFieldDefinition {

    protected String orderColumn;

    public ListFieldDefinition(String name, String foreignKeyColumn,
            EntityDefinition referencedEntity, String orderColumn, boolean lazy) {
        super(name, foreignKeyColumn, referencedEntity, lazy);
        this.orderColumn = orderColumn;
    }

    public String getOrderColumn() {
        return orderColumn;
    }
}
