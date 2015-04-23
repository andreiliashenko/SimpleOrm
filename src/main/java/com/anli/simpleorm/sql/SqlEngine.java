package com.anli.simpleorm.sql;

import com.anli.simpleorm.definitions.EntityDefinition;
import com.anli.simpleorm.descriptors.EntityDescriptor;
import com.anli.simpleorm.descriptors.UnitDescriptorManager;
import com.anli.simpleorm.queries.EntityQueryCache;
import com.anli.simpleorm.queries.UnitQueryManager;
import com.anli.simpleorm.reflective.EntityProcessor;
import com.anli.sqlexecution.execution.SqlExecutor;
import com.anli.sqlexecution.handling.ResultSetHandler;
import com.anli.sqlexecution.handling.TransformingResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class SqlEngine {

    protected final UnitQueryManager queryManager;
    protected final UnitDescriptorManager descriptorManager;
    protected final SqlExecutor executor;

    public SqlEngine(UnitQueryManager queryManager,
            UnitDescriptorManager descriptorManager, SqlExecutor executor) {
        this.queryManager = queryManager;
        this.descriptorManager = descriptorManager;
        this.executor = executor;
    }

    public <E> void insertAnemicEntity(E entity, Object primaryKey, Class<E> entityClass) {
        EntityQueryCache queryCache = getQueryCache(entityClass);
        List<String> insertQueries = queryCache.getInsertQueries();
        ListIterator<String> queryIterator = insertQueries.listIterator(insertQueries.size());
        insertAnemicPart(getDefinition(entityClass), queryIterator, primaryKey);
    }

    protected void insertAnemicPart(EntityDefinition currentDefinition,
            ListIterator<String> queryIter, Object primaryKey) {
        String query = queryIter.previous();
        EntityDefinition parentDefinition = currentDefinition.getParentEntity();
        if (parentDefinition != null) {
            insertAnemicPart(parentDefinition, queryIter, primaryKey);
        }
        getExecutor().executeUpdate(query, Collections.singletonList(primaryKey));
    }

    public boolean exists(Object entity, Class entityClass) {
        Class keyClass = getDescriptor(entityClass).getPrimaryKeyClass();
        Object primaryKey = getProcessor(entityClass).getPrimaryKey(entity);
        String query = getQueryCache(entityClass).getSelectKeyQuery();
        List keys = getExecutor().executeSelect(query, Collections.singletonList(primaryKey),
                new KeyCollector(keyClass));
        return !keys.isEmpty();
    }

    public void delete(Object primaryKey, Class entityClass) {
        String deleteQuery = getQueryCache(entityClass).getDeleteQuery();
        getExecutor().executeUpdate(deleteQuery, Collections.singletonList(primaryKey));
    }

    protected UnitQueryManager getQueryManager() {
        return queryManager;
    }

    protected EntityQueryCache getQueryCache(Class entityClass) {
        return getQueryManager().getEntityQueryCache(entityClass);
    }

    protected EntityDefinition getDefinition(Class entityClass) {
        return getDescriptor(entityClass).getDefinition();
    }

    protected UnitDescriptorManager getDescriptorManager() {
        return descriptorManager;
    }

    protected EntityDescriptor getDescriptor(Class entityClass) {
        return getDescriptorManager().getDescriptor(entityClass);
    }

    protected SqlExecutor getExecutor() {
        return executor;
    }

    protected EntityProcessor getProcessor(Class entityClass) {
        return getDescriptor(entityClass).getProcessor();
    }

    protected static class KeyCollector implements ResultSetHandler<List> {

        protected final Class keyClass;

        public KeyCollector(Class keyClass) {
            this.keyClass = keyClass;
        }

        @Override
        public List handle(TransformingResultSet resultSet) throws SQLException {
            List keys = new LinkedList();
            while (resultSet.next()) {
                keys.add(resultSet.getValue(1, keyClass));
            }
            return keys;
        }
    }
}
