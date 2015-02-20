package com.anli.simpleorm.handling;

import java.util.Collection;

public interface EntityGateway<Entity> {

    EntityBuilder<Entity> getBuilder(String entityName);

    Object extractSingle(Entity entity, String entityName, String fieldName);

    Object extractFullReference(Entity entity, String entityName, String fieldName);

    Collection extractCollectionKeys(Entity entity, String entityName, String fieldName);

    Collection extractFullCollection(Entity entity, String entityName, String fieldName);

    Collection extractCollectionByKeys(Entity entity, String entityName, String fieldName,
            Collection keys);

    void setCollectionField(Entity entity, String entityName, String fieldName, Collection value);
}
