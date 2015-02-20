package com.anli.simpleorm.queries;

import com.anli.simpleorm.definitions.EntityDefinition;
import com.anli.simpleorm.definitions.PrimitiveDefinition;

public class CharacterFieldQueryCache extends SingleFieldQueryCache {

    protected String selectFullByRegexp;
    protected String selectKeysByRegexp;

    public CharacterFieldQueryCache(EntityDefinition mainDefinition, EntityDefinition fieldEntityDefinition,
            PrimitiveDefinition fieldDefinition, MySqlQueryBuilder queryBuilder) {
        super(mainDefinition, fieldEntityDefinition, fieldDefinition, queryBuilder);
    }

    public String getSelectFullByRegexpQuery() {
        if (selectFullByRegexp == null) {
            selectFullByRegexp = buildSelectByRegexpQuery(true);
        }
        return selectFullByRegexp;
    }

    public String getSelectKeysByRegexpQuery() {
        if (selectKeysByRegexp == null) {
            selectKeysByRegexp = buildSelectByRegexpQuery(false);
        }
        return selectKeysByRegexp;
    }

    protected String buildSelectByRegexpQuery(boolean full) {
        return queryBuilder.buildSelectEntityByStringRegexp(mainDefinition, fieldEntityDefinition,
                fieldDefinition, full);
    }
}
