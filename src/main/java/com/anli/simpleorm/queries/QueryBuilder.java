package com.anli.simpleorm.queries;

import com.anli.simpleorm.definitions.CollectionFieldDefinition;
import com.anli.simpleorm.definitions.EntityDefinition;
import java.util.List;

public interface QueryBuilder {

    String FOREIGN_KEY_BINDING = "foreignKey";
    String LINKED_KEYS_BINDING = "linkedKeys";
    String PARENT_JOIN_KEY_BINDING = "parentJoinKey";

    QueryDescriptor buildSelectEntityQuery(EntityDefinition definition);

    QueryDescriptor buildSelectEntitiesByKeysQuery(EntityDefinition definition);

    QueryDescriptor buildSelectExistingKeysQuery(EntityDefinition definition);

    List<QueryDescriptor> buildInsertFullEntityQueries(EntityDefinition definition);

    List<QueryDescriptor> buildInsertAnemicEntityQueries(EntityDefinition definition);

    QueryDescriptor buildUpdateEntityQuery(EntityDefinition definition);

    QueryDescriptor buildDeleteEntityQuery(EntityDefinition definition);

    QueryDescriptor buildSelectCollectionKeysQuery(CollectionFieldDefinition fieldDefinition);

    QueryDescriptor buildLinkCollectionQuery(CollectionFieldDefinition fieldDefinition);

    QueryDescriptor buildClearCollectionQuery(CollectionFieldDefinition fieldDefinition);

    QueryDescriptor buildUnlinkCollectionQuery(CollectionFieldDefinition fieldDefinition);
}
