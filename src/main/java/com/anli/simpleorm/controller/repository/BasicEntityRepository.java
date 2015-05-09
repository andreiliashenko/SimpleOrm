package com.anli.simpleorm.controller.repository;

import com.anli.simpleorm.controller.EntityRepository;
import com.anli.simpleorm.controller.basic.AbstractEntityController;
import com.anli.simpleorm.definitions.CollectionDefinition;
import com.anli.simpleorm.definitions.EntityDefinition;
import com.anli.simpleorm.descriptors.UnitDescriptorManager;
import com.anli.simpleorm.exceptions.NonExistentEntitiesException;
import com.anli.simpleorm.exceptions.ReflectionException;
import com.anli.simpleorm.sql.SqlEngine;
import java.util.Collection;

public class BasicEntityRepository extends AbstractEntityController implements EntityRepository {

    public BasicEntityRepository(UnitDescriptorManager descriptorManager, SqlEngine sqlEngine) {
        super(descriptorManager, sqlEngine);
    }

    @Override
    protected <E> E getEntityInstance(Class<E> entityClass) {
        try {
            return entityClass.newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new ReflectionException("Could not instantiate entity", ex);
        }
    }

    @Override
    protected <E> void storeNewEntity(E entity, Object primaryKey, Class<E> entityClass) {
        getSqlEngine().insertAnemicEntity(primaryKey, entityClass);
    }

    @Override
    protected <E> Class<E> resolveEntityClass(Class<E> effectiveClass) {
        return effectiveClass;
    }

    @Override
    protected void remove(Object entity, Object primaryKey, Class entityClass) {
        checkConsistency(entity, entityClass);
        getSqlEngine().delete(primaryKey, entityClass);
    }

    @Override
    public <E> E getByPrimaryKey(Object primaryKey, Class<E> entityClass) {
        return loadEntityByPrimaryKey(primaryKey, entityClass, getLoadingContext());
    }

    @Override
    public <E> void save(E entity) {
        Class entityClass = resolveEntityClass(entity.getClass());
        checkConsistency(entity, entityClass);
    }

    @Override
    public <E> void pull(E entity, String lazyFieldName) {
    }

    protected void checkConsistency(Object entity, Class entityClass) {
        Object key = getProcessor(entityClass).getPrimaryKey(entity);
        if (!getSqlEngine().exists(key, entityClass)) {
            throw new NonExistentEntitiesException(entity);
        }
    }

    protected void checkReferencesConsistency(Object entity, Class entityClass) {
    //    EntityDefinition
    }

    @Override
    protected LoadingContext getLoadingContext() {
        return null;
    }

    @Override
    protected Collection getCollectionValue(EntityDefinition entityDefinition,
            CollectionDefinition fieldDefinition, Object foreignKey, LoadingContext loadingContext) {
        return null;
    }
}
