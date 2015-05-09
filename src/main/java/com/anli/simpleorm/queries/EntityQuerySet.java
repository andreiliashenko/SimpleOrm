package com.anli.simpleorm.queries;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityQuerySet {

    protected QueryDescriptor selectQuery;
    protected QueryDescriptor selectByKeysQuery;
    protected QueryDescriptor selectExistingKeysQuery;
    protected List<QueryDescriptor> insertFullQueries;
    protected List<QueryDescriptor> insertAnemicQueries;
    protected QueryDescriptor updateQuery;
    protected QueryDescriptor deleteQuery;

    protected final Map<String, CollectionQuerySet> collectionSets;

    public EntityQuerySet() {
        collectionSets = new HashMap<>();
    }

    public QueryDescriptor getSelectQuery() {
        return selectQuery;
    }

    public void setSelectQuery(QueryDescriptor selectQuery) {
        this.selectQuery = selectQuery;
    }

    public QueryDescriptor getSelectByKeysQuery() {
        return selectByKeysQuery;
    }

    public void setSelectByKeysQuery(QueryDescriptor selectByKeysQuery) {
        this.selectByKeysQuery = selectByKeysQuery;
    }

    public QueryDescriptor getSelectExistingKeysQuery() {
        return selectExistingKeysQuery;
    }

    public void setSelectExistingKeysQuery(QueryDescriptor selectExistingKeysQuery) {
        this.selectExistingKeysQuery = selectExistingKeysQuery;
    }

    public List<QueryDescriptor> getInsertFullQueries() {
        return insertFullQueries;
    }

    public void setInsertFullQueries(List<QueryDescriptor> insertFullQueries) {
        this.insertFullQueries = insertFullQueries;
    }

    public List<QueryDescriptor> getInsertAnemicQueries() {
        return insertAnemicQueries;
    }

    public void setInsertAnemicQueries(List<QueryDescriptor> insertAnemicQueries) {
        this.insertAnemicQueries = insertAnemicQueries;
    }

    public QueryDescriptor getUpdateQuery() {
        return updateQuery;
    }

    public void setUpdateQuery(QueryDescriptor updateQuery) {
        this.updateQuery = updateQuery;
    }

    public QueryDescriptor getDeleteQuery() {
        return deleteQuery;
    }

    public void setDeleteQuery(QueryDescriptor deleteQuery) {
        this.deleteQuery = deleteQuery;
    }

    public CollectionQuerySet getCollectionSet(String field) {
        return collectionSets.get(field);
    }

    public void addCollectionSet(String field, CollectionQuerySet set) {
        collectionSets.put(field, set);
    }
}
