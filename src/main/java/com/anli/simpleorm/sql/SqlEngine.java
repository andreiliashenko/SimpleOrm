package com.anli.simpleorm.sql;

import com.anli.simpleorm.descriptors.CollectionFieldDescriptor;
import com.anli.simpleorm.descriptors.EntityDescriptor;
import com.anli.simpleorm.descriptors.FieldDescriptor;
import com.anli.simpleorm.descriptors.UnitDescriptorManager;
import com.anli.simpleorm.queries.EntityQuerySet;
import com.anli.simpleorm.queries.QueryDescriptor;
import com.anli.sqlexecution.execution.SqlExecutor;
import com.anli.sqlexecution.handling.ResultSetHandler;
import com.anli.sqlexecution.handling.TransformingResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.anli.simpleorm.queries.QueryBuilder.FOREIGN_KEY_BINDING;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;

public class SqlEngine {

    protected final UnitDescriptorManager descriptorManager;
    protected final SqlExecutor executor;

    public SqlEngine(UnitDescriptorManager descriptorManager, SqlExecutor executor) {
        this.descriptorManager = descriptorManager;
        this.executor = executor;
    }

    protected void executeUpdate(QueryDescriptor query, Map<String, Object> parameters) {
        query = resolveQuery(query, parameters);
        List paramList = resolveParameters(query, parameters);
        getExecutor().executeUpdate(query.getQuery(), paramList);
    }

    protected <T> T executeSelect(QueryDescriptor query, Map<String, Object> parameters,
            ResultSetHandler<T> handler) {
        query = resolveQuery(query, parameters);
        List paramList = resolveParameters(query, parameters);
        return getExecutor().executeSelect(query.getQuery(), paramList, handler);
    }

    protected QueryDescriptor resolveQuery(QueryDescriptor sourceDescriptor,
            Map<String, Object> parameters) {
        return sourceDescriptor;
    }

    protected List resolveParameters(QueryDescriptor query, Map<String, Object> parameters) {
        Object[] paramArray = new Object[parameters.size()];
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            String binding = entry.getKey();
            Object value = entry.getValue();
            int index = query.getParameterBinding(binding);
            paramArray[index - 1] = value;
        }
        return asList(paramArray);
    }

    public <E> void insertAnemicEntity(Object primaryKey, Class<E> entityClass) {
        EntityQuerySet querySet = getQuerySet(entityClass);
        List<QueryDescriptor> insertQueries = querySet.getInsertAnemicQueries();
        for (QueryDescriptor query : insertQueries) {
            String pkBinding = getDescriptor(entityClass).getPrimaryKeyBinding();
            executeUpdate(query, singletonMap(pkBinding, primaryKey));
        }
    }

    public boolean exists(Object primaryKey, Class entityClass) {
        return getNonExistentKeys(asList(primaryKey), entityClass).isEmpty();
    }

    public Set getNonExistentKeys(Collection primaryKeys, Class entityClass) {
        Set keySet = new HashSet(primaryKeys);
        Class keyClass = getDescriptor(entityClass).getPrimaryKeyClass();
        QueryDescriptor query = getQuerySet(entityClass).getSelectExistingKeysQuery();
        String pkBinding = getDescriptor(entityClass).getPrimaryKeyBinding();
        List existentKeys = executeSelect(query, (Map) singletonMap(pkBinding, primaryKeys),
                new KeyCollector(keyClass));
        keySet.removeAll(existentKeys);
        return keySet;
    }

    public void delete(Object primaryKey, Class entityClass) {
        QueryDescriptor deleteQuery = getQuerySet(entityClass).getDeleteQuery();
        String pkBinding = getDescriptor(entityClass).getPrimaryKeyBinding();
        executeUpdate(deleteQuery, singletonMap(pkBinding, primaryKey));
    }

    public DataRow getByPrimaryKey(Object primaryKey, Class entityClass) {
        QueryDescriptor selectQuery = getQuerySet(entityClass).getSelectQuery();
        String pkBinding = getDescriptor(entityClass).getPrimaryKeyBinding();
        List<DataRow> rows = executeSelect(selectQuery,
                singletonMap(pkBinding, primaryKey),
                new EntityDataSelector(getDescriptor(entityClass), selectQuery));
        return !rows.isEmpty() ? rows.iterator().next() : null;
    }

    public List getCollectionKeys(CollectionFieldDescriptor field, Object foreignKey,
            String collectionField) {
        Class keyClass = getDescriptor(field.getElementClass()).getPrimaryKeyClass();
        QueryDescriptor query = field.getQuerySet().getSelectCollectionKeysQuery();
        return executeSelect(query, singletonMap(FOREIGN_KEY_BINDING, foreignKey),
                new KeyCollector(keyClass));
    }

    protected EntityQuerySet getQuerySet(Class entityClass) {
        return getDescriptor(entityClass).getQuerySet();
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

    protected class KeyCollector implements ResultSetHandler<List> {

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

    protected class EntityDataSelector implements ResultSetHandler<List<DataRow>> {

        protected final EntityDescriptor descriptor;
        protected final QueryDescriptor query;

        public EntityDataSelector(EntityDescriptor descriptor, QueryDescriptor query) {
            this.descriptor = descriptor;
            this.query = query;
        }

        @Override
        public List handle(TransformingResultSet resultSet) throws SQLException {
            List<DataRow> rows = new LinkedList<>();
            while (resultSet.next()) {
                rows.add(getRow(resultSet));
            }
            return rows;
        }

        protected DataRow getRow(TransformingResultSet resultSet) throws SQLException {
            DataRow row = getNewRow();
            for (FieldDescriptor field : getDescriptor().getSingleFields()) {
                for (String binding : field.getAllBindings()) {
                    row.put(binding, resultSet.getValue(getQuery().getResultBinding(binding),
                            field.getFieldClass()));
                }
            }
            for (EntityDescriptor child : getDescriptor().getChildrenDescriptors()) {
                populateRowForChild(resultSet, child, row);
            }
            return row;
        }

        protected void populateRowForChild(TransformingResultSet resultSet,
                EntityDescriptor descriptor, DataRow row) throws SQLException {
            for (FieldDescriptor field : descriptor.getSingleFields()) {
                String binding = field.getBinding();
                row.put(binding, resultSet.getValue(getQuery().getResultBinding(binding),
                        field.getFieldClass()));
            }
            for (EntityDescriptor childDescriptor : descriptor.getChildrenDescriptors()) {
                populateRowForChild(resultSet, childDescriptor, row);
            }
        }

        protected EntityDescriptor getDescriptor() {
            return descriptor;
        }

        protected QueryDescriptor getQuery() {
            return query;
        }

        protected DataRow getNewRow() {
            return new DataRow();
        }
    }
}
