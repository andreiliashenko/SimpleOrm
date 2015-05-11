package com.anli.simpleorm.controller.basic;

import com.anli.simpleorm.controller.EntityController;
import com.anli.simpleorm.controller.PrimaryKeyGenerator;
import com.anli.simpleorm.descriptors.CollectionFieldDescriptor;
import com.anli.simpleorm.descriptors.EntityDescriptor;
import com.anli.simpleorm.descriptors.FieldDescriptor;
import com.anli.simpleorm.descriptors.UnitDescriptorManager;
import com.anli.simpleorm.reflective.EntityProcessor;
import com.anli.simpleorm.sql.DataRow;
import com.anli.simpleorm.sql.SqlEngine;
import java.util.Collection;

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
        storeNewEntity(entity, primaryKey, entityClass);
        return entity;
    }

    @Override
    public void remove(Object entity) {
        Class entityClass = resolveEntityClass(entity.getClass());
        EntityProcessor processor = getProcessor(entityClass);
        Object primaryKey = processor.getPrimaryKey(entity);
        remove(entity, primaryKey, entityClass);
    }

    protected <E> E loadEntityByPrimaryKey(Object primaryKey, Class<E> entityClass,
            LoadingContext context) {
        DataRow dataRow = getSqlEngine().getByPrimaryKey(primaryKey, entityClass);
        return buildEntity(entityClass, dataRow, context);
    }

    protected <E> E buildEntity(Class<E> entityClass, DataRow dataRow, LoadingContext context) {
        Class<? extends E> effectiveClass = getEffectiveClass(getDescriptor(entityClass), dataRow);
        E instance = getEntityInstance(effectiveClass);
        populateEntity(getDescriptor(effectiveClass), instance, dataRow, getLoadingContext());
        return instance;
    }

    protected <E> void populateEntity(EntityDescriptor descriptor, E entity,
            DataRow dataRow, LoadingContext context) {
        EntityProcessor processor = descriptor.getProcessor();
        Object entityKey = dataRow.get(descriptor.getPrimaryKeyBinding());
        for (FieldDescriptor primitiveField : descriptor.getPrimitiveFields()) {
            setPrimitiveField(primitiveField, processor, entity,
                    dataRow.get(primitiveField.getBinding()));
        }
        for (FieldDescriptor referenceField : descriptor.getReferenceFields()) {
            setReferenceField(referenceField, processor, entity,
                    dataRow.get(referenceField.getBinding()), context);
        }
        for (CollectionFieldDescriptor collectionField : descriptor.getCollectionFields()) {
            Collection collectionValue = getCollectionValue(collectionField, entityKey, context);
            setCollectionField(collectionField, processor, entity, collectionValue);
        }
    }

    protected <E> void setReferenceField(FieldDescriptor field, EntityProcessor processor,
            E entity, Object key, LoadingContext context) {
        Object referenceValue = context.get(field.getFieldClass(), key, field.isLazy());
        processor.setField(entity, field.getName(), referenceValue, field.isLazy());
    }

    protected <E> void setPrimitiveField(FieldDescriptor field, EntityProcessor processor,
            E entity, Object value) {
        processor.setField(entity, field.getName(), value);
    }

    protected <E> void setCollectionField(CollectionFieldDescriptor field, EntityProcessor processor,
            E entity, Collection collectionValue) {
        processor.setField(entity, field.getName(), collectionValue, field.isLazy());
    }

    protected <E> Class<? extends E> getEffectiveClass(EntityDescriptor descriptor, DataRow dataRow) {
        String keyBinding = descriptor.getPrimaryKeyBinding();
        if (dataRow.get(keyBinding) == null) {
            return null;
        }
        for (EntityDescriptor child : descriptor.getChildrenDescriptors()) {
            Class resolvedClass = getEffectiveClass(child, dataRow);
            if (resolvedClass != null) {
                return resolvedClass;
            }
        }
        return descriptor.getEntityClass();
    }

    protected <E> void storeEntity(E entity) {

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

    protected SqlEngine getSqlEngine() {
        return sqlEngine;
    }

    protected abstract <E> E getEntityInstance(Class<E> entityClass);

    protected abstract <E> void storeNewEntity(E entity, Object primaryKey, Class<E> entityClass);

    protected abstract <E> Class<E> resolveEntityClass(Class<E> effectiveClass);

    protected abstract void remove(Object entity, Object primaryKey, Class entityClass);

    protected abstract LoadingContext getLoadingContext();

    protected abstract Collection getCollectionValue(CollectionFieldDescriptor descriptor,
            Object foreignKey, LoadingContext loadingContext);

    protected static interface LoadingContext {

        <E> E get(Class<E> entityClass, Object primaryKey, boolean lazy);

        <E> Collection<E> get(Class<E> entityClass, Collection keys, boolean lazy);
    }
}
