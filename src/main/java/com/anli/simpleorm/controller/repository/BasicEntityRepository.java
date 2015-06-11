package com.anli.simpleorm.controller.repository;

import com.anli.simpleorm.cache.HierarchicalCache;
import com.anli.simpleorm.controller.EntityRepository;
import com.anli.simpleorm.controller.basic.AbstractEntityController;
import com.anli.simpleorm.descriptors.CollectionFieldDescriptor;
import com.anli.simpleorm.descriptors.EntityDescriptor;
import com.anli.simpleorm.descriptors.FieldDescriptor;
import com.anli.simpleorm.descriptors.UnitDescriptorManager;
import com.anli.simpleorm.exceptions.LazyFieldException;
import com.anli.simpleorm.exceptions.NonExistentEntitiesException;
import com.anli.simpleorm.lazy.Loader;
import com.anli.simpleorm.reflective.EntityProcessor;
import com.anli.simpleorm.reflective.repository.RepositoryProcessor;
import com.anli.simpleorm.sql.DataRow;
import com.anli.simpleorm.sql.SqlEngine;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;

public class BasicEntityRepository extends AbstractEntityController implements EntityRepository {

    protected static final Loader ERROR_LAZY_LOADER = new ErrorLoader();

    public BasicEntityRepository(UnitDescriptorManager descriptorManager, SqlEngine sqlEngine) {
        super(descriptorManager, sqlEngine);
    }

    @Override
    protected <E> void storeNewEntity(E entity, Object primaryKey, Class<E> entityClass) {
        getSqlEngine().insertAnemicEntity(primaryKey, entityClass);
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
        checkNotNull(entity, "Entity cannot be null");
        Class entityClass = resolveEntityClass(entity.getClass());
        checkConsistency(entity, entityClass);
        Collection inconsistent = getInconsistentReferences(entity, entityClass);
        inconsistent.addAll(getInconsistentCollectionElements(entity, entityClass));
        if (!inconsistent.isEmpty()) {
            throw new NonExistentEntitiesException(inconsistent);
        }
        storeEntity(entity);
    }

    @Override
    public <E> void pull(E entity, String lazyFieldName) {
        checkNotNull(entity);
        Class entityClass = resolveEntityClass(entity.getClass());
        checkConsistency(entity, entityClass);
        EntityDescriptor descriptor = getDescriptor(entityClass);
        EntityProcessor processor = descriptor.getProcessor();
        FieldDescriptor field = descriptor.getField(lazyFieldName);
        checkState(field.isLazy());
        if (!processor.isLazyClean(entity, lazyFieldName)) {
            return;
        }
        if (field instanceof CollectionFieldDescriptor) {
            Object entityKey = processor.getPrimaryKey(entity);
            Collection value = getCollectionValue((CollectionFieldDescriptor) field,
                    entityKey, false, getLoadingContext());
            setCollectionField((CollectionFieldDescriptor) field, processor,
                    entity, value, false);
        } else {
            Object key = processor.getLazyKey(entity, lazyFieldName);
            setReferenceField(field, processor, entity, key, getLoadingContext(), false);
        }
    }

    protected void checkConsistency(Object entity, Class entityClass) {
        if (isInconsistent(entity, entityClass)) {
            throw new NonExistentEntitiesException(entity);
        }
    }

    protected boolean isInconsistent(Object entity, Class entityClass) {
        Object key = getProcessor(entityClass).getPrimaryKey(entity);
        return !getSqlEngine().exists(key, entityClass);
    }

    protected Collection getInconsistentReferences(Object entity, Class entityClass) {
        LinkedList inconsistent = new LinkedList();
        EntityDescriptor descriptor = getDescriptor(entityClass);
        EntityProcessor processor = descriptor.getProcessor();
        for (FieldDescriptor field : descriptor.getReferenceFields()) {
            Class fieldClass = field.getFieldClass();
            String fieldName = field.getName();
            if (processor.isLazyClean(entity, fieldName)) {
                Object key = processor.getLazyKey(entity, fieldName);
                if (key != null && !getSqlEngine().exists(key, fieldClass)) {
                    processor.setLazyReference(entity, fieldName, null);
                }
            } else {
                Object reference = processor.getField(entity, fieldName);
                if (reference != null && isInconsistent(reference, fieldClass)) {
                    inconsistent.add(reference);
                }
            }
        }
        return inconsistent;
    }

    protected Collection getInconsistentCollectionElements(Object entity, Class entityClass) {
        LinkedList inconsistent = new LinkedList();
        EntityDescriptor descriptor = getDescriptor(entityClass);
        EntityProcessor processor = descriptor.getProcessor();
        for (CollectionFieldDescriptor field : descriptor.getCollectionFields()) {
            Class elementClass = field.getElementClass();
            String fieldName = field.getName();
            if (!processor.isLazyClean(entity, fieldName)) {
                Collection keys = getCollectionKeys(field, processor, entity);
                Set nonExistentKeys = !keys.isEmpty()
                        ? getSqlEngine().getNonExistentKeys(keys, elementClass)
                        : emptySet();
                if (!keys.isEmpty()) {
                    EntityProcessor fieldProcessor = getDescriptor(elementClass).getProcessor();
                    Collection collection = (Collection) fieldProcessor.getField(entity, fieldName);
                    Collection nonExistent = getNonExistentEntitiesByKeys(collection,
                            nonExistentKeys, fieldProcessor);
                    inconsistent.addAll(nonExistent);
                }
            }
        }
        return inconsistent;
    }

    protected Collection getNonExistentEntitiesByKeys(Collection entities, Set keys,
            EntityProcessor processor) {
        ArrayList nonExistentEntities = new ArrayList(keys.size());
        for (Object entity : entities) {
            Object key = processor.getPrimaryKey(entity);
            if (keys.contains(key)) {
                nonExistentEntities.add(entity);
            }
        }
        return nonExistentEntities;
    }

    @Override
    protected <E> void setCollectionField(CollectionFieldDescriptor field, EntityProcessor processor, E entity,
            Collection collectionValue, boolean isLazy) {
        if (isLazy) {
            ((RepositoryProcessor) processor).markLazyCollection(entity, field.getName());
        }
        super.setCollectionField(field, processor, entity, collectionValue, isLazy);
    }

    @Override
    protected LoadingContext getLoadingContext() {
        return new AtomicContext();
    }

    @Override
    protected Collection getCollectionValue(CollectionFieldDescriptor descriptor, Object foreignKey,
            boolean isLazy, LoadingContext loadingContext) {
        if (isLazy) {
            return null;
        }
        List keys = getSqlEngine().getCollectionKeys(descriptor, foreignKey);
        return loadingContext.get(descriptor.getElementClass(), descriptor.getFieldClass(),
                keys, isLazy);
    }

    @Override
    protected Loader getLoader(Class entityClass) {
        return ERROR_LAZY_LOADER;
    }

    protected static class ErrorLoader implements Loader {

        @Override
        public Object get(Object key) {
            throw new LazyFieldException();
        }

        @Override
        public Object extractKey(Object value) {
            throw new LazyFieldException();
        }

    }

    protected class AtomicContext implements LoadingContext {

        protected final HierarchicalCache cache;

        public AtomicContext() {
            this.cache = new HierarchicalCache();
        }

        @Override
        public <E> void put(Class<E> entityClass, Object primaryKey, E entity) {
            cache.put(entityClass, primaryKey, entity);
        }

        @Override
        public <E> E get(Class<E> entityClass, Object primaryKey, boolean lazy) {
            if (lazy || primaryKey == null) {
                return null;
            }
            E entity = (E) cache.get(entityClass, primaryKey);
            if (entity == null) {
                entity = loadEntityByPrimaryKey(primaryKey, entityClass, this);
                cache.put(resolveEntityClass(entityClass), primaryKey, entity);
            }
            return entity;
        }

        @Override
        public <E> Collection<E> get(Class<E> entityClass, Class collectionClass,
                Collection keys, boolean lazy) {
            if (lazy) {
                return null;
            }
            SortedMap<Integer, E> loaded = new TreeMap<>();
            SortedMap<Integer, Object> unloaded = new TreeMap<>();
            int index = 0;
            for (Object key : keys) {
                E entity = (E) cache.get(entityClass, key);
                if (entity == null) {
                    unloaded.put(index, key);
                } else {
                    loaded.put(index, entity);
                }
                index++;
            }
            Map<Object, E> loadedEntities = getEntityMap(entityClass, unloaded.values());
            for (Map.Entry<Integer, Object> entry : unloaded.entrySet()) {
                loaded.put(entry.getKey(), loadedEntities.get(entry.getValue()));
            }
            return getResultCollection(collectionClass, loaded);
        }

        protected <E> Map<Object, E> getEntityMap(Class<E> elementClass, Collection keys) {
            if (keys == null || keys.isEmpty()) {
                return emptyMap();
            }
            Map<Object, DataRow> dataMap = getSqlEngine().getCollectionData(elementClass, keys);
            Map<Object, E> entityMap = new HashMap<>((int) (dataMap.size() / 0.75));
            for (Map.Entry<Object, DataRow> entry : dataMap.entrySet()) {
                entityMap.put(entry.getKey(), buildEntity(elementClass, entry.getValue(), this));
            }
            return entityMap;
        }

        protected <E> Collection<E> getResultCollection(Class collectionClass,
                SortedMap<Integer, E> entities) {
            if (List.class.equals(collectionClass) || Collection.class.equals(collectionClass)) {
                return new ArrayList(entities.values());
            } else if (Set.class.equals(collectionClass)) {
                return new HashSet(entities.values());
            } else {
                throw new IllegalArgumentException();
            }
        }
    }
}
