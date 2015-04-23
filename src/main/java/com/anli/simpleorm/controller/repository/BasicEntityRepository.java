package com.anli.simpleorm.controller.repository;

import com.anli.simpleorm.controller.EntityRepository;
import com.anli.simpleorm.controller.basic.AbstractEntityController;
import com.anli.simpleorm.descriptors.UnitDescriptorManager;
import com.anli.simpleorm.exceptions.NonExistentEntitiesException;
import com.anli.simpleorm.exceptions.ReflectionException;
import com.anli.simpleorm.sql.SqlEngine;

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
    protected <E> void storeNew(E entity, Object primaryKey, Class<E> entityClass) {
        getSqlEngine().insertAnemicEntity(entity, primaryKey, entityClass);
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
    }

    @Override
    public <E> E save(E entity) {
    }

    @Override
    public <E> E pull(E entity, String lazyFieldName) {
    }

    protected void checkConsistency(Object entity, Class entityClass) {
        if (!getSqlEngine().exists(entity, entityClass)) {
            throw new NonExistentEntitiesException(entity);
        }
    }
}
