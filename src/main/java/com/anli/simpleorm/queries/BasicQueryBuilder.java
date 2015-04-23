package com.anli.simpleorm.queries;

import com.anli.simpleorm.definitions.CollectionDefinition;
import com.anli.simpleorm.definitions.EntityDefinition;
import com.anli.simpleorm.definitions.ListDefinition;
import java.util.Map;

public interface BasicQueryBuilder {

    String buildDeleteEntityQuery(EntityDefinition definition);

    String buildInsertEntityQuery(EntityDefinition definition);

    String buildLinkCollectionQueryTemplate(CollectionDefinition fieldDefinition);

    String buildListOrderingSubquery(ListDefinition listField, int size);

    String buildParametersList(int size);

    String buildUpdateEntityQuery(EntityDefinition definition);

    Map<String, Integer> getKeysIndices(EntityDefinition definition);
}
