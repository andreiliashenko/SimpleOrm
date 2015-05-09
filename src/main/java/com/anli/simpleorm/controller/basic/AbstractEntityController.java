package com.anli.simpleorm.controller.basic;

import com.anli.simpleorm.controller.EntityController;
import com.anli.simpleorm.controller.PrimaryKeyGenerator;
import com.anli.simpleorm.definitions.CollectionDefinition;
import com.anli.simpleorm.definitions.EntityDefinition;
import com.anli.simpleorm.definitions.FieldDefinition;
import com.anli.simpleorm.definitions.ReferenceDefinition;
import com.anli.simpleorm.descriptors.EntityDescriptor;
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
        //Class<? extends E> effectiveClass = getEffectiveClass(entityClass, dataRow);
        //E instance = getEntityInstance(effectiveClass);
        //populateEntity(getDefinition(effectiveClass), getProcessor(effectiveClass),
        //        instance, dataRow, getLoadingContext());
       //return instance;
        return null;
    }

    protected <E> void populateEntity(EntityDefinition definition, EntityProcessor processor,
            E entity, DataRow dataRow, LoadingContext context) {
        EntityDefinition parentDefinition = definition.getParentDefinition();
        if (parentDefinition != null) {
            populateEntity(parentDefinition, processor, entity, dataRow, context);
        }
        //Object entityKey = dataRow.get(definition.getPrimaryKeyDataRowKey());
        for (FieldDefinition singleField : definition.getSingleFields()) {
            //Object value = dataRow.get(definition.getDataRowKey(singleField.getName()));
            if (singleField instanceof ReferenceDefinition) {
             //   setReferenceField((ReferenceDefinition) singleField,
                //        processor, entity, value, context);
            } else {
             //   setPrimitiveField(singleField, processor, entity, value);
            }
        }
        for (CollectionDefinition collectionField : definition.getCollectionFields()) {
           // Collection collectionValue = getCollectionValue(definition, collectionField,
           //         entityKey, context);
          //  setCollectionField(collectionField, processor, entity, collectionValue);
        }
    }

    protected <E> void setReferenceField(ReferenceDefinition fieldDefinition,
            EntityProcessor processor, E entity, Object key, LoadingContext context) {
        Class referenceClass = fieldDefinition.getReferencedEntity().getEntityClass();
        boolean lazy = fieldDefinition.isLazy();
        Object referenceValue = context.get(referenceClass, key, lazy);
        processor.setField(entity, fieldDefinition.getName(), referenceValue, lazy);
    }

    protected <E> void setPrimitiveField(FieldDefinition fieldDefinition,
            EntityProcessor processor, E entity, Object value) {
        processor.setField(entity, fieldDefinition.getName(), value);
    }

    protected <E> void setCollectionField(CollectionDefinition fieldDefinition,
            EntityProcessor processor, E entity, Collection collectionValue) {
        processor.setField(entity, fieldDefinition.getName(), collectionValue,
                fieldDefinition.isLazy());
    }

    /*protected <E> Class<? extends E> getEffectiveClass(Class<E> entityClass, DataRow dataRow) {
        EntityDefinition definition = getDefinition(entityClass);
        for (EntityDefinition childDefinition : definition.getChildrenDefinitions()) {
            String rowKey = childDefinition.getPrimaryKeyDataRowKey();
            if (dataRow.get(rowKey) != null) {
                return getEffectiveClass(childDefinition.getEntityClass(), dataRow);
            }
        }
        return entityClass;
    }
*/
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

    protected abstract Collection getCollectionValue(EntityDefinition entityDefinition,
            CollectionDefinition fieldDefinition, Object foreignKey, LoadingContext loadingContext);

    protected static interface LoadingContext {

        <E> E get(Class<E> entityClass, Object primaryKey, boolean lazy);

        <E> Collection<E> get(Class<E> entityClass, Collection keys, boolean lazy);
    }
}
