package com.anli.simpleorm.queries;

import com.anli.simpleorm.definitions.EntityDefinition;
import com.anli.simpleorm.definitions.FieldDefinition;

public class SingleFieldQueryCache extends FieldQueryCache {

    protected String selectFullByIsNull;
    protected String selectKeysByIsNull;

    public SingleFieldQueryCache(EntityDefinition mainDefinition, EntityDefinition fieldEntityDefinition,
            FieldDefinition fieldDefinition, MySqlQueryBuilder queryBuilder) {
        super(mainDefinition, fieldEntityDefinition, fieldDefinition, queryBuilder);
    }

    public String getSelectFullByIsNullQuery() {
        if (selectFullByIsNull == null) {
            selectFullByIsNull = buildSelectByIsNullQuery(true);
        }
        return selectFullByIsNull;
    }

    public String getSelectKeysByIsNullQuery() {
        if (selectKeysByIsNull == null) {
            selectKeysByIsNull = buildSelectByIsNullQuery(false);
        }
        return selectKeysByIsNull;
    }

    protected String buildSelectByIsNullQuery(boolean full) {
        return queryBuilder.buildSelectEntityBySingleFieldNull(mainDefinition, fieldEntityDefinition,
                fieldDefinition, full);
    }
}
