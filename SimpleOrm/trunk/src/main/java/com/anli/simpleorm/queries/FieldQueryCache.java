package com.anli.simpleorm.queries;

import com.anli.simpleorm.definitions.EntityDefinition;
import com.anli.simpleorm.definitions.FieldDefinition;

public class FieldQueryCache {

    protected final EntityDefinition mainDefinition;
    protected final EntityDefinition fieldEntityDefinition;
    protected final FieldDefinition fieldDefinition;
    protected final MySqlQueryBuilder queryBuilder;

    protected String selectFullByEqualsOrContains;
    protected String selectKeysByEqualsOrContains;
    protected String selectFullByAnyTemplate;
    protected String selectKeysByAnyTemplate;

    public FieldQueryCache(EntityDefinition mainDefinition, EntityDefinition fieldEntityDefinition,
            FieldDefinition fieldDefinition, MySqlQueryBuilder queryBuilder) {
        this.mainDefinition = mainDefinition;
        this.fieldEntityDefinition = fieldEntityDefinition;
        this.fieldDefinition = fieldDefinition;
        this.queryBuilder = queryBuilder;
    }

    public String getSelectFullByEqualsOrContainsQuery() {
        if (selectFullByEqualsOrContains == null) {
            selectFullByEqualsOrContains = buildSelectByEqualsOrContainsQuery(true);
        }
        return selectFullByEqualsOrContains;
    }

    public String getSelectKeysByEqualsOrContainsQuery() {
        if (selectKeysByEqualsOrContains == null) {
            selectKeysByEqualsOrContains = buildSelectByEqualsOrContainsQuery(false);
        }
        return selectKeysByEqualsOrContains;
    }

    public String getSelectFullByAnyQuery(int size) {
        if (selectFullByAnyTemplate == null) {
            selectFullByAnyTemplate = buildSelectByAnyTemplate(true);
        }
        return String.format(selectFullByAnyTemplate, queryBuilder.buildParametersList(size));
    }

    public String getSelectKeysByAnyQuery(int size) {
        if (selectKeysByAnyTemplate == null) {
            selectKeysByAnyTemplate = buildSelectByAnyTemplate(false);
        }
        return String.format(selectKeysByAnyTemplate, queryBuilder.buildParametersList(size));
    }

    protected String buildSelectByEqualsOrContainsQuery(boolean full) {
        return queryBuilder.buildSelectEntityBySingleFieldEquals(mainDefinition, fieldEntityDefinition,
                fieldDefinition, full);
    }

    protected String buildSelectByAnyTemplate(boolean full) {
        return queryBuilder.buildSelectEntityBySingleFieldInListTemplate(mainDefinition, fieldEntityDefinition,
                fieldDefinition, full);
    }
}
