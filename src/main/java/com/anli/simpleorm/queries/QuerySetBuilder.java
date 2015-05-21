package com.anli.simpleorm.queries;

import com.anli.simpleorm.definitions.CollectionFieldDefinition;
import com.anli.simpleorm.definitions.EntityDefinition;

public class QuerySetBuilder {

    protected final QueryBuilder builder;
    
    public QuerySetBuilder(QueryBuilder builder) {
        this.builder = builder;
    }

    public EntityQuerySet buildQuerySet(EntityDefinition definition) {
        EntityQuerySet querySet = new EntityQuerySet();
        querySet.setSelectQuery(builder.buildSelectEntityQuery(definition));
        querySet.setSelectByKeysQuery(builder.buildSelectEntitiesByKeysQuery(definition));
        querySet.setSelectExistingKeysQuery(builder.buildSelectExistingKeysQuery(definition));
        querySet.setInsertAnemicQueries(builder.buildInsertAnemicEntityQueries(definition));
        querySet.setInsertFullQueries(builder.buildInsertFullEntityQueries(definition));
        querySet.setUpdateQuery(builder.buildUpdateEntityQuery(definition));
        querySet.setDeleteQuery(builder.buildDeleteEntityQuery(definition));
        return querySet;
    }
    
    public CollectionQuerySet buildCollectionQuerySet(CollectionFieldDefinition definition) {
        CollectionQuerySet querySet = new CollectionQuerySet();
        querySet.setSelectCollectionKeysQuery(builder.buildSelectCollectionKeysQuery(definition));
        querySet.setLinkCollectionQuery(builder.buildLinkCollectionQuery(definition));
        querySet.setUnlinkCollectionQuery(builder.buildUnlinkCollectionQuery(definition));
        querySet.setClearCollectionQuery(builder.buildClearCollectionQuery(definition));
        return querySet;
    }
}
