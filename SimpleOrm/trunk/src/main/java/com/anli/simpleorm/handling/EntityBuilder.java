package com.anli.simpleorm.handling;

public interface EntityBuilder<Entity> {

    EntityBuilder startBuilding();

    EntityBuilder setSingle(String entityName, String fieldName, Object fieldValue);

    Entity build();
}
