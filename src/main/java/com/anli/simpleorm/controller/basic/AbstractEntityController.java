package com.anli.simpleorm.controller.basic;

import com.anli.simpleorm.controller.EntityController;
import com.anli.simpleorm.controller.PrimaryKeyGenerator;
import com.anli.simpleorm.descriptors.CollectionFieldDescriptor;
import com.anli.simpleorm.descriptors.EntityDescriptor;
import com.anli.simpleorm.descriptors.FieldDescriptor;
import com.anli.simpleorm.descriptors.UnitDescriptorManager;
import com.anli.simpleorm.lazy.LazyValue;
import com.anli.simpleorm.lazy.Loader;
import com.anli.simpleorm.reflective.EntityProcessor;
import com.anli.simpleorm.sql.DataRow;
import com.anli.simpleorm.sql.SqlEngine;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Collections.emptyList;

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
        EntityProcessor processor = getProcessor(entityClass);
        E entity = processor.getInstance();
        processor.setPrimaryKey(entity, primaryKey);
        storeNewEntity(entity, primaryKey, entityClass);
        return entity;
    }

    @Override
    public void remove(Object entity) {
        checkNotNull(entity);
        Class entityClass = resolveEntityClass(entity.getClass());
        EntityProcessor processor = getProcessor(entityClass);
        Object primaryKey = processor.getPrimaryKey(entity);
        remove(entity, primaryKey, entityClass);
    }

    protected <E> E loadEntityByPrimaryKey(Object primaryKey, Class<E> entityClass,
            LoadingContext context) {
        DataRow dataRow = getSqlEngine().getByPrimaryKey(primaryKey, entityClass);
        if (dataRow == null) {
            return null;
        }
        return buildEntity(entityClass, dataRow, context);
    }

    protected <E> E buildEntity(Class<E> entityClass, DataRow dataRow, LoadingContext context) {
        Class<? extends E> effectiveClass = getEffectiveClass(getDescriptor(entityClass), dataRow);
        E instance = getProcessor(effectiveClass).getInstance();
        populateEntity(getDescriptor(effectiveClass), instance, dataRow, getLoadingContext());
        return instance;
    }

    protected <E> void populateEntity(EntityDescriptor descriptor, E entity,
            DataRow dataRow, LoadingContext context) {
        EntityProcessor processor = descriptor.getProcessor();
        Object entityKey = dataRow.get(descriptor.getPrimaryKeyBinding());
        context.put(descriptor.getEntityClass(), entityKey, entity);
        for (FieldDescriptor primitiveField : descriptor.getPrimitiveFields()) {
            setPrimitiveField(primitiveField, processor, entity,
                    dataRow.get(primitiveField.getBinding()));
        }
        for (FieldDescriptor referenceField : descriptor.getReferenceFields()) {
            setReferenceField(referenceField, processor, entity,
                    dataRow.get(referenceField.getBinding()), context, referenceField.isLazy());
        }
        for (CollectionFieldDescriptor collectionField : descriptor.getCollectionFields()) {
            Collection collectionValue = getCollectionValue(collectionField, entityKey,
                    collectionField.isLazy(), context);
            setCollectionField(collectionField, processor, entity, collectionValue,
                    collectionField.isLazy());
        }
    }

    protected <E> void setReferenceField(FieldDescriptor field, EntityProcessor processor,
            E entity, Object key, LoadingContext context, boolean isLazy) {
        if (isLazy) {
            processor.setLazyReference(entity, field.getName(),
                    new LazyValue(getLoader(field.getFieldClass()), key));
        }
        Object referenceValue = key != null ? context.get(field.getFieldClass(),
                key, isLazy) : null;
        processor.setField(entity, field.getName(), referenceValue);
    }

    protected <E> void setPrimitiveField(FieldDescriptor field, EntityProcessor processor,
            E entity, Object value) {
        processor.setField(entity, field.getName(), value);
    }

    protected <E> void setCollectionField(CollectionFieldDescriptor field, EntityProcessor processor,
            E entity, Collection collectionValue, boolean isLazy) {
        processor.setField(entity, field.getName(), collectionValue);
    }

    protected <E> Class<? extends E> getEffectiveClass(EntityDescriptor descriptor, DataRow dataRow) {
        return getEffectiveClass(descriptor, dataRow, descriptor.getEntityClass());
    }

    protected <E> Class<? extends E> getEffectiveClass(EntityDescriptor descriptor, DataRow dataRow,
            Class root) {
        String keyBinding = descriptor.getParentJoinBinding();
        if (dataRow.get(keyBinding) == null) {
            return root;
        }
        for (EntityDescriptor child : descriptor.getChildrenDescriptors()) {
            Class resolvedClass = getEffectiveClass(child, dataRow, null);
            if (resolvedClass != null) {
                return resolvedClass;
            }
        }
        return descriptor.getEntityClass();
    }

    protected <E> void storeEntity(E entity) {
        Map<String, Object> storedParameters = new HashMap<>();
        Class entityClass = resolveEntityClass(entity.getClass());
        EntityDescriptor descriptor = getDescriptor(entityClass);
        EntityProcessor processor = descriptor.getProcessor();
        for (FieldDescriptor field : descriptor.getPrimitiveFields()) {
            Object value = getPrimitiveField(field, processor, entity);
            storedParameters.put(field.getBinding(), value);
        }
        for (FieldDescriptor field : descriptor.getReferenceFields()) {
            Object value = getReferenceKey(field, processor, entity);
            storedParameters.put(field.getBinding(), value);
        }
        getSqlEngine().updateEntity(storedParameters, entityClass);
        Object entityKey = processor.getPrimaryKey(entity);
        for (CollectionFieldDescriptor field : descriptor.getCollectionFields()) {
            if (field.isLazy() && processor.isLazyClean(entity, field.getName())) {
                continue;
            }
            Collection value = getCollectionKeys(field, processor, entity);
            getSqlEngine().updateCollectionLinkage(field, value, entityKey);
        }
    }

    protected <E> Object getPrimitiveField(FieldDescriptor field, EntityProcessor processor,
            E entity) {
        return processor.getField(entity, field.getName());
    }

    protected <E> Object getReferenceKey(FieldDescriptor field, EntityProcessor processor,
            E entity) {
        if (processor.isLazyClean(entity, field.getName())) {
            return processor.getLazyKey(entity, field.getName());
        }
        Object reference = processor.getField(entity, field.getName());
        if (reference == null) {
            return null;
        }
        return getProcessor(resolveEntityClass(reference.getClass())).getPrimaryKey(reference);
    }

    protected <E> Collection getCollectionKeys(CollectionFieldDescriptor field, EntityProcessor processor,
            E entity) {
        Collection collection = (Collection) processor.getField(entity, field.getName());
        if (collection == null) {
            return emptyList();
        }
        ArrayList list = new ArrayList(collection.size());
        EntityProcessor elementProcessor = getDescriptor(field.getElementClass()).getProcessor();
        for (Object element : collection) {
            Object key = elementProcessor.getPrimaryKey(element);
            list.add(key);
        }
        return list;
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

    protected <E> Class<E> resolveEntityClass(Class<E> effectiveClass) {
        return descriptorManager.getBasicEntityClass(effectiveClass);
    }

    protected abstract <E> void storeNewEntity(E entity, Object primaryKey, Class<E> entityClass);

    protected abstract void remove(Object entity, Object primaryKey, Class entityClass);

    protected abstract LoadingContext getLoadingContext();

    protected abstract Collection getCollectionValue(CollectionFieldDescriptor descriptor,
            Object foreignKey, boolean isLazy, LoadingContext loadingContext);

    protected abstract Loader getLoader(Class entityClass);

    protected static interface LoadingContext {

        <E> void put(Class<E> entityClass, Object primaryKey, E entity);

        <E> E get(Class<E> entityClass, Object primaryKey, boolean lazy);

        <E> Collection<E> get(Class<E> entityClass, Class collectionClass, Collection keys, boolean lazy);
    }
}
