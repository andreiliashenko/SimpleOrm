package com.anli.simpleorm.handling;

import java.util.Collection;
import java.util.Map;

public class SuperEntityGateway<Entity> implements EntityGateway<Entity> {
    
    protected final Map<Class<? extends Entity>, EntityGateway<? extends Entity>> mapping;
    
    public SuperEntityGateway (Map<Class<? extends Entity>, EntityGateway<? extends Entity>> mapping) {
        this.mapping = mapping;
    }

    @Override
    public EntityBuilder<Entity> getBuilder(String entityName) {
        EntityBuilder<Entity> builder = null;
        for (EntityGateway<? extends Entity> subGateway : mapping.values()) {
            builder = (EntityBuilder) subGateway.getBuilder(entityName);
            if (builder != null) {
                break;
            }
        }
        return builder;
    }

    @Override
    public Object extractSingle(Entity entity, String entityName, String fieldName) {
        return getConcreteGateway(entity).extractSingle(entity, entityName, fieldName);
    }

    @Override
    public Object extractFullReference(Entity entity, String entityName, String fieldName) {
        return getConcreteGateway(entity).extractFullReference(entity, entityName, fieldName);
    }

    @Override
    public Collection extractCollectionKeys(Entity entity, String entityName, String fieldName) {
        return getConcreteGateway(entity).extractCollectionKeys(entity, entityName, fieldName);
    }

    @Override
    public Collection extractFullCollection(Entity entity, String entityName, String fieldName) {
        return getConcreteGateway(entity).extractFullCollection(entity, entityName, fieldName);
    }

    @Override
    public Collection extractCollectionByKeys(Entity entity, String entityName, String fieldName, Collection keys) {
        return getConcreteGateway(entity).extractCollectionByKeys(entity, entityName, fieldName, keys);
    }

    @Override
    public void setCollectionField(Entity entity, String entityName, String fieldName, Collection value) {
        getConcreteGateway(entity).setCollectionField(entity, entityName, fieldName, value);
    }
    
    protected EntityGateway<Entity> getConcreteGateway(Entity entity) {
        Class<? extends Entity> entityClass = (Class) entity.getClass();
        EntityGateway<Entity> gateway = (EntityGateway) mapping.get(entityClass);
        if (gateway != null) {
            return gateway;
        }
        for (Class<? extends Entity> clazz : mapping.keySet()) {
            if (clazz.isAssignableFrom(entityClass)) {
                return (EntityGateway) mapping.get(clazz);
            }
        }
        return null;
    }
}
