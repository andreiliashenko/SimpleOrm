package com.anli.simpleorm.handling;

import com.anli.simpleorm.definitions.CollectionDefinition;
import com.anli.simpleorm.definitions.EntityDefinition;
import com.anli.simpleorm.definitions.FieldDefinition;
import com.anli.simpleorm.definitions.ReferenceDefinition;
import com.anli.simpleorm.exceptions.NonExistentEntitiesException;
import com.anli.simpleorm.queries.CharacterFieldQueryCache;
import com.anli.simpleorm.queries.CollectionFieldQueryCache;
import com.anli.simpleorm.queries.ComparableFieldQueryCache;
import com.anli.simpleorm.queries.EntityQueryCache;
import com.anli.simpleorm.queries.FieldQueryCache;
import com.anli.simpleorm.queries.MySqlQueryBuilder;
import com.anli.simpleorm.queries.SingleFieldQueryCache;
import com.anli.sqlexecution.execution.SqlExecutor;
import com.anli.sqlexecution.handling.ResultSetHandler;
import com.anli.sqlexecution.handling.TransformingResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

public class EntityHandler<Entity> {

    protected final EntityDefinition definition;
    protected final EntityQueryCache queryCache;
    protected final EntityGateway<Entity> gateway;
    protected final EntityHandlerFactory handlerFactory;
    protected final SqlExecutor executor;

    protected final EntitySelector selector;
    protected final KeyCollector keyCollector;

    public EntityHandler(EntityDefinition definition, MySqlQueryBuilder queryBuilder, EntityGateway<Entity> gateway,
            EntityHandlerFactory handlerFactory, SqlExecutor executor) {
        this.definition = definition;
        this.queryCache = new EntityQueryCache(definition, queryBuilder);
        this.gateway = gateway;
        this.executor = executor;
        this.handlerFactory = handlerFactory;
        this.selector = new EntitySelector();
        this.keyCollector = new KeyCollector();
    }

    public Entity insertEntity(Object primaryKey) {
        EntityBuilder<Entity> builder = gateway.getBuilder(definition.getName()).startBuilding();
        List<String> insertQueries = queryCache.getInsertQueries();
        ListIterator<String> queryIterator = insertQueries.listIterator(insertQueries.size());
        insertSingle(definition, builder, queryIterator, primaryKey);
        return builder.build();
    }

    public Entity selectEntity(Object primaryKey) {
        String selectQuery = queryCache.getSelectQuery();
        List<Entity> found = executor.executeSelect(selectQuery, Arrays.asList(primaryKey), selector);
        return found.isEmpty() ? null : found.iterator().next();
    }

    public boolean isKeyPresent(Object primaryKey) {
        return !collectKeysByEqualsOrContains(definition.getPrimaryKey().getName(),
                primaryKey).isEmpty();
    }

    protected List selectKeysByKeyList(Collection keys) {
        return collectKeysByAny(definition.getPrimaryKey().getName(),
                new ArrayList(keys));
    }

    public List<Entity> selectAllEntities() {
        String selectQuery = queryCache.getSelectAllQuery();
        return executor.executeSelect(selectQuery, Collections.emptyList(), selector);
    }

    public List collectAllKeys() {
        String collectQuery = queryCache.getSelectAllKeysQuery();
        return executor.executeSelect(collectQuery, Collections.emptyList(), keyCollector);
    }

    public List<Entity> selectEntitiesByEqualsOrContains(String field, Object value) {
        FieldQueryCache cache = queryCache.getFieldQueryCache(field);
        String selectQuery;
        List parameters;
        if (value != null) {
            selectQuery = cache.getSelectFullByEqualsOrContainsQuery();
            parameters = Arrays.asList(getParameter(definition.getFieldEntity(field)
                    .getField(field), value));
        } else if (cache instanceof CollectionFieldQueryCache) {
            return Collections.emptyList();
        } else {
            selectQuery = ((SingleFieldQueryCache) cache).getSelectFullByIsNullQuery();
            parameters = Collections.emptyList();
        }
        return executor.executeSelect(selectQuery, parameters, selector);
    }

    public List collectKeysByEqualsOrContains(String field, Object value) {
        FieldQueryCache cache = queryCache.getFieldQueryCache(field);
        String collectQuery;
        List parameters;
        if (value != null) {
            collectQuery = cache.getSelectKeysByEqualsOrContainsQuery();
            parameters = Arrays.asList(getParameter(definition.getFieldEntity(field)
                    .getField(field), value));
        } else if (cache instanceof CollectionFieldQueryCache) {
            return Collections.emptyList();
        } else {
            collectQuery = ((SingleFieldQueryCache) cache).getSelectKeysByIsNullQuery();
            parameters = Collections.emptyList();
        }
        return executor.executeSelect(collectQuery, parameters, keyCollector);
    }

    public List<Entity> selectEntitiesByAny(String field, Collection values) {
        if (values == null || values.isEmpty()) {
            return Collections.emptyList();
        }
        String selectQuery = queryCache.getFieldQueryCache(field)
                .getSelectFullByAnyQuery(values.size());
        return executor.executeSelect(selectQuery,
                getParameterList(definition.getFieldEntity(field).getField(field), values),
                selector);
    }

    public List collectKeysByAny(String field, Collection values) {
        if (values == null || values.isEmpty()) {
            return Collections.emptyList();
        }
        String collectQuery = queryCache.getFieldQueryCache(field)
                .getSelectKeysByAnyQuery(values.size());
        return executor.executeSelect(collectQuery,
                getParameterList(definition.getFieldEntity(field).getField(field), values), keyCollector);
    }

    public List<Entity> selectEntitiesByRegexp(String field, String regexp) {
        String selectQuery = ((CharacterFieldQueryCache) queryCache.getFieldQueryCache(field))
                .getSelectFullByRegexpQuery();
        return executor.executeSelect(selectQuery, Arrays.asList(regexp), selector);
    }

    public List collectKeysByRegexp(String field, String regexp) {
        String collectQuery = ((CharacterFieldQueryCache) queryCache.getFieldQueryCache(field))
                .getSelectKeysByRegexpQuery();
        return executor.executeSelect(collectQuery, Arrays.asList(regexp), keyCollector);
    }

    public List<Entity> selectEntitiesByRange(String field, Object left, boolean leftStrict,
            Object right, boolean rightStrict) {
        if (left == null && right == null) {
            return selectAllEntities();
        }
        String selectQuery;
        List params;
        ComparableFieldQueryCache cache
                = (ComparableFieldQueryCache) queryCache.getFieldQueryCache(field);
        if (left != null && right != null) {
            selectQuery = cache.getSelectFullByClosedRangeQuery(leftStrict, rightStrict);
            params = Arrays.asList(left, right);
        } else {
            boolean isLeft = (left != null);
            boolean strict = isLeft ? leftStrict : rightStrict;
            selectQuery = cache.getSelectFullByOpenRangeQuery(isLeft, strict);
            params = Arrays.asList(isLeft ? left : right);
        }
        return executor.executeSelect(selectQuery, params, selector);
    }

    public List collectKeysByRange(String field, Object left, boolean leftStrict,
            Object right, boolean rightStrict) {
        if (left == null && right == null) {
            return collectAllKeys();
        }
        String collectQuery;
        List params;
        ComparableFieldQueryCache cache
                = (ComparableFieldQueryCache) queryCache.getFieldQueryCache(field);
        if (left != null && right != null) {
            collectQuery = cache.getSelectKeysByClosedRangeQuery(leftStrict, rightStrict);
            params = Arrays.asList(left, right);
        } else {
            boolean isLeft = (left != null);
            boolean strict = isLeft ? leftStrict : rightStrict;
            collectQuery = cache.getSelectKeysByOpenRangeQuery(isLeft, strict);
            params = Arrays.asList(isLeft ? left : right);
        }
        return executor.executeSelect(collectQuery, params, keyCollector);
    }

    public List<Entity> selectEntitiesByNamedQuery(String queryName, Collection parameters) {
        List transformedParameters = new LinkedList();
        List<Integer> sizes = new LinkedList<>();
        for (Object parameter : parameters) {
            if (parameter instanceof Collection) {
                Collection collectionParam = (Collection) parameter;
                transformedParameters.addAll(collectionParam);
                sizes.add(collectionParam.size());
            } else {
                transformedParameters.add(parameter);
            }
        }
        String selectQuery = sizes.isEmpty() ? queryCache.getSelectNamedQuery(queryName)
                : queryCache.getSelectNamedQuery(queryName, sizes);
        return executor.executeSelect(selectQuery, transformedParameters, selector);
    }

    public List collectKeysByNamedQuery(String queryName, Collection parameters) {
        List transformedParameters = new LinkedList();
        List<Integer> sizes = new LinkedList<>();
        for (Object parameter : parameters) {
            if (parameter instanceof Collection) {
                Collection collectionParam = (Collection) parameter;
                transformedParameters.addAll(collectionParam);
                sizes.add(collectionParam.size());
            } else {
                transformedParameters.add(parameter);
            }
        }
        String collectQuery = sizes.isEmpty() ? queryCache.getSelectKeysNamedQuery(queryName)
                : queryCache.getSelectKeysNamedQuery(queryName, sizes);
        return executor.executeSelect(collectQuery, transformedParameters, keyCollector);
    }

    public void pullCollection(Entity entity, String field) {
        String selectCollectionQuery = ((CollectionFieldQueryCache) queryCache
                .getFieldQueryCache(field))
                .getSelectCollectionQuery();
        String entityName = definition.getName();
        Object primaryKey = getPrimaryKey(entity);
        String collectionEntityName = definition.getFieldEntity(field).getCollectionField(field)
                .getReferencedEntity().getName();
        EntityHandler collectionHandler = handlerFactory.getHandler(collectionEntityName);
        List collection = (List) executor.executeSelect(selectCollectionQuery, Arrays.asList(primaryKey),
                collectionHandler.selector);
        gateway.setCollectionField(entity, entityName, field, collection);
    }

    public void updateEntity(Entity entity) {
        checkConsistency(entity);
        String updateSinglesQuery = queryCache.getUpdateQuery();
        List updateSinglesParameters = new LinkedList();
        addUpdateParameters(definition, updateSinglesParameters,
                entity, true, true);
        updateSinglesParameters.add(getPrimaryKey(entity));
        executor.executeUpdate(updateSinglesQuery, updateSinglesParameters);
        updateAllCollectionLinks(definition, entity, true, true);
    }

    public void deleteEntity(Entity entity) {
        checkEntityConsistency(entity);
        String deleteQuery = queryCache.getDeleteQuery();
        Object primaryKey = getPrimaryKey(entity);
        executor.executeUpdate(deleteQuery, Arrays.asList(primaryKey));
    }

    protected void insertSingle(EntityDefinition currentDefinition, EntityBuilder<Entity> builder,
            ListIterator<String> queryIter, Object primaryKey) {
        String query = queryIter.previous();
        EntityDefinition parentDefinition = currentDefinition.getParentEntity();
        if (parentDefinition != null) {
            insertSingle(parentDefinition, builder, queryIter, primaryKey);
        }
        builder.setSingle(currentDefinition.getName(),
                currentDefinition.getPrimaryKey().getName(), primaryKey);
        executor.executeUpdate(query, Arrays.asList(primaryKey));
    }

    protected void addUpdateParameters(EntityDefinition currentDefinition,
            List parameters, Entity entity, boolean withChildren, boolean withParent) {
        EntityDefinition parentDefinition = currentDefinition.getParentEntity();
        if (parentDefinition != null && withParent) {
            addUpdateParameters(parentDefinition, parameters, entity, false, true);
        }
        String entityName = currentDefinition.getName();
        for (FieldDefinition field : currentDefinition.getSingleFields()) {
            Object value = gateway.extractSingle(entity, entityName, field.getName());
            parameters.add(value);
        }
        if (!withChildren) {
            return;
        }
        for (EntityDefinition childDefinition : currentDefinition.getChildrenEntities()) {
            addUpdateParameters(childDefinition, parameters, entity, true, false);
        }
    }

    protected void updateAllCollectionLinks(EntityDefinition entityDefinition, Entity entity,
            boolean withChildren, boolean withParent) {
        EntityDefinition parentDefinition = entityDefinition.getParentEntity();
        if (parentDefinition != null && withParent) {
            updateAllCollectionLinks(parentDefinition, entity, false, true);
        }
        for (CollectionDefinition field : entityDefinition.getCollectionFields()) {
            updateCollectionLink(entityDefinition, entity, field.getName());
        }
        if (!withChildren) {
            return;
        }
        for (EntityDefinition childDefinition : entityDefinition.getChildrenEntities()) {
            updateAllCollectionLinks(childDefinition, entity, true, false);
        }
    }

    protected void updateCollectionLink(EntityDefinition entityDefinition, Entity entity, String fieldName) {
        String entityName = entityDefinition.getName();
        Collection collectionKeys = gateway.extractCollectionKeys(entity, entityName, fieldName);
        Object primaryKey = getPrimaryKey(entity);
        if (collectionKeys == null) {
            return;
        }
        CollectionFieldQueryCache fieldQueryCache
                = (CollectionFieldQueryCache) queryCache.getFieldQueryCache(fieldName);
        int size = collectionKeys.size();
        int paramSize = size + 1;
        if (!collectionKeys.isEmpty()) {

            String linkQuery = fieldQueryCache.getLinkCollectionQuery(size);
            List linkParamList = new ArrayList(paramSize);
            linkParamList.addAll(collectionKeys);
            linkParamList.add(primaryKey);
            executor.executeUpdate(linkQuery, linkParamList);
        }
        String unlinkQuery = fieldQueryCache.getUnlinkCollectionQuery(size);
        List unlinkParamList = new ArrayList(paramSize);
        unlinkParamList.add(primaryKey);
        unlinkParamList.addAll(collectionKeys);
        executor.executeUpdate(unlinkQuery, unlinkParamList);
    }

    protected void checkConsistency(Entity entity) {
        checkEntityConsistency(entity);
        List inconsistentEntities = new LinkedList();
        checkReferencesConsistency(inconsistentEntities, definition, entity, true, true);
        if (!inconsistentEntities.isEmpty()) {
            throw new NonExistentEntitiesException(inconsistentEntities);
        }
    }

    protected void checkEntityConsistency(Entity entity) {
        Object primaryKey = getPrimaryKey(entity);
        if (!isKeyPresent(primaryKey)) {
            throw new NonExistentEntitiesException(Arrays.asList(entity));
        }
    }

    protected void checkReferencesConsistency(List inconsistent, EntityDefinition entityDefinition, Entity entity,
            boolean withChildren, boolean withParent) {
        EntityDefinition parentDefinition = entityDefinition.getParentEntity();
        if (parentDefinition != null && withParent) {
            checkReferencesConsistency(inconsistent, parentDefinition, entity,
                    false, true);
        }
        String entityName = entityDefinition.getName();
        for (FieldDefinition field : entityDefinition.getSingleFields()) {
            if (field instanceof ReferenceDefinition) {
                checkReferenceConsistency((ReferenceDefinition) field, entity,
                        entityName, inconsistent);
            }
        }
        for (CollectionDefinition field : entityDefinition.getCollectionFields()) {
            checkCollectionConsistency(field, entity, entityName, inconsistent);
        }
        if (!withChildren) {
            return;
        }
        for (EntityDefinition childDefinition : entityDefinition.getChildrenEntities()) {
            checkReferencesConsistency(inconsistent, childDefinition, entity, false, true);
        }
    }

    protected void checkReferenceConsistency(ReferenceDefinition field, Entity entity, String entityName,
            List inconsistent) {
        String referenceName = field.getReferencedEntity().getName();
        String fieldName = field.getName();
        Object key = gateway.extractSingle(entity, entityName, fieldName);
        if (key != null) {
            EntityHandler refHandler = handlerFactory.getHandler(referenceName);
            if (!refHandler.isKeyPresent(key)) {
                inconsistent.add(gateway.extractFullReference(entity, entityName, fieldName));
            }
        }
    }

    protected void checkCollectionConsistency(CollectionDefinition field, Entity entity, String entityName,
            List inconsistent) {
        String collectionName = field.getReferencedEntity().getName();
        String fieldName = field.getName();
        Collection keys = gateway.extractCollectionKeys(entity, entityName, fieldName);
        Set inconsistentKeys = new HashSet(keys);
        if (keys != null) {
            EntityHandler collectionHandler = handlerFactory.getHandler(collectionName);
            inconsistentKeys.removeAll(collectionHandler.selectKeysByKeyList(keys));
            inconsistent.addAll(gateway.extractCollectionByKeys(entity, entityName, fieldName,
                    inconsistentKeys));
        }
    }

    protected Object getPrimaryKey(Entity entity) {
        return gateway.extractSingle(entity, definition.getName(),
                definition.getPrimaryKey().getName());
    }

    protected Object getParameter(FieldDefinition field, Object value) {
        if (field instanceof ReferenceDefinition) {
            return handlerFactory.getHandler(((ReferenceDefinition) field).getReferencedEntity()
                    .getName()).getPrimaryKey(value);
        }
        return value;
    }

    protected List getParameterList(FieldDefinition field, Collection values) {
        if (field instanceof ReferenceDefinition) {
            EntityHandler handler = handlerFactory.getHandler(((ReferenceDefinition) field).getReferencedEntity()
                    .getName());
            ArrayList keys = new ArrayList(values.size());
            for (Object value : values) {
                keys.add(handler.getPrimaryKey(value));
            }
            return keys;
        }
        return new ArrayList(values);
    }

    protected class EntitySelector implements ResultSetHandler<List<Entity>> {

        @Override
        public List<Entity> handle(TransformingResultSet resultSet) throws SQLException {
            List<Entity> entities = new LinkedList<>();
            Map<String, Map> referencedEntities = new HashMap<>();
            while (resultSet.next()) {
                entities.add(getEntity(resultSet, referencedEntities));
            }
            return entities;
        }

        protected Entity getEntity(TransformingResultSet resultSet,
                Map<String, Map> referencedEntities) throws SQLException {
            String actualEntity = getActualEntityName(resultSet, definition, queryCache.getKeysIndices());
            EntityBuilder<Entity> builder = gateway.getBuilder(actualEntity).startBuilding();
            populateEntity(definition, resultSet, builder, referencedEntities,
                    0, true, true);
            return builder.build();
        }

        protected String getActualEntityName(TransformingResultSet resultSet,
                EntityDefinition currentDefinition, Map<String, Integer> indices) throws SQLException {
            for (EntityDefinition child : currentDefinition.getChildrenEntities()) {
                String childName = getActualEntityName(resultSet, child, indices);
                if (childName != null) {
                    return childName;
                }
            }
            String name = currentDefinition.getName();
            int index = indices.get(name);
            Object key = resultSet.getValue(index, currentDefinition.getPrimaryKey().getJavaClass());
            return key != null ? name : null;
        }

        protected int populateEntity(EntityDefinition entityDefinition, TransformingResultSet resultSet,
                EntityBuilder builder, Map<String, Map> referencedEntities, int lastIndex,
                boolean withChildren, boolean withParent) throws SQLException {
            EntityDefinition parentDefinition = entityDefinition.getParentEntity();
            if (parentDefinition != null && withParent) {
                lastIndex = populateEntity(parentDefinition, resultSet, builder,
                        referencedEntities, lastIndex, false, true);
            }
            String entityName = entityDefinition.getName();
            for (FieldDefinition fieldDefinition : entityDefinition.getSingleFields()) {
                lastIndex++;
                Object value = resultSet.getValue(lastIndex, fieldDefinition.getJavaClass());
                if (fieldDefinition instanceof ReferenceDefinition) {
                    String referenceName = ((ReferenceDefinition) fieldDefinition).getReferencedEntity().getName();
                    value = getReference(referencedEntities, referenceName, value);
                }
                builder.setSingle(entityName, fieldDefinition.getName(), value);
            }
            if (!withChildren) {
                return lastIndex;
            }
            for (EntityDefinition childDefinition : entityDefinition.getChildrenEntities()) {
                lastIndex = populateEntity(childDefinition, resultSet, builder,
                        referencedEntities, lastIndex, true, false);
            }
            return lastIndex;
        }

        protected Object getReference(Map<String, Map> map, String entityName, Object key) {
            if (key == null) {
                return null;
            }
            Map entities = map.get(entityName);
            if (entities == null) {
                entities = new HashMap();
                map.put(entityName, entities);
            }
            Object entity = entities.get(key);
            if (entity == null) {
                entity = pullReference(entityName, key);
            }
            return entity;
        }

        protected Object pullReference(String entityName, Object key) {
            EntityHandler handler = handlerFactory.getHandler(entityName);
            return handler.selectEntity(key);
        }
    }

    protected class KeyCollector implements ResultSetHandler<List> {

        @Override
        public List handle(TransformingResultSet resultSet) throws SQLException {
            Class keyClass = definition.getPrimaryKey().getJavaClass();
            List keys = new LinkedList();
            while (resultSet.next()) {
                keys.add(resultSet.getValue(1, keyClass));
            }
            return keys;
        }
    }
}
