package com.anli.simpleorm.controller.basic;

import com.anli.simpleorm.controller.EntityController;
import com.anli.simpleorm.controller.PrimaryKeyGenerator;
import com.anli.simpleorm.definitions.EntityDefinition;
import com.anli.simpleorm.descriptors.EntityDescriptor;
import com.anli.simpleorm.descriptors.UnitDescriptorManager;
import com.anli.simpleorm.queries.EntityQueryCache;
import com.anli.simpleorm.queries.UnitQueryManager;
import com.anli.simpleorm.reflective.EntityProcessor;
import com.anli.simpleorm.sql.SqlEngine;

public abstract class AbstractEntityController implements EntityController {

    protected final UnitDescriptorManager descriptorManager;
    protected final SqlEngine sqlEngine;

    protected AbstractEntityController(UnitDescriptorManager descriptorManager,
            SqlEngine sqlEngine) {
        this.descriptorManager = descriptorManager;
        this.sqlEngine = sqlEngine;
    }

    @Override
    public <E> E create(Class<E> entityClass) {
        PrimaryKeyGenerator keyGenerator = getKeyGenerator(entityClass);
        Object primaryKey = keyGenerator.getPrimaryKey(entityClass);
        E entity = getEntityInstance(entityClass);
        EntityProcessor processor = getProcessor(entityClass);
        processor.setPrimaryKey(entity, primaryKey);
        storeNew(entity, primaryKey, entityClass);
        return entity;
    }

    @Override
    public void remove(Object entity) {
        Class entityClass = resolveEntityClass(entity.getClass());
        EntityProcessor processor = getProcessor(entityClass);
        Object primaryKey = processor.getPrimaryKey(entity);
        remove(entity, primaryKey, entityClass);
    }

    protected UnitDescriptorManager getDescriptorManager() {
        return descriptorManager;
    }

    protected EntityDescriptor getDescriptor(Class entityClass) {
        return getDescriptorManager().getDescriptor(entityClass);
    }

    protected PrimaryKeyGenerator getKeyGenerator(Class entityClass) {
        return getDescriptor(entityClass).getPrimaryKeyGenerator();
    }

    protected EntityProcessor getProcessor(Class entityClass) {
        return getDescriptor(entityClass).getProcessor();
    }

    protected EntityDefinition getDefinition(Class entityClass) {
        return getDescriptor(entityClass).getDefinition();
    }

    protected SqlEngine getSqlEngine() {
        return sqlEngine;
    }

    protected abstract <E> E getEntityInstance(Class<E> entityClass);

    protected abstract <E> void storeNew(E entity, Object primaryKey, Class<E> entityClass);

    protected abstract <E> Class<E> resolveEntityClass(Class<E> effectiveClass);

    protected abstract void remove(Object entity, Object primaryKey, Class entityClass);
}
