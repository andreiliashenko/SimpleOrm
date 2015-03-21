package com.anli.simpleorm.queries;

import com.anli.simpleorm.definitions.EntityDefinition;
import com.anli.simpleorm.definitions.PrimitiveDefinition;

public class ComparableFieldQueryCache extends SingleFieldQueryCache {

    protected static final String GREATER = ">";
    protected static final String LESS = "<";
    protected static final String EQUALS = "=";

    protected String selectFullByOpenRangeTemplate;
    protected String selectKeysByOpenRangeTemplate;
    protected String selectFullByClosedRangeTemplate;
    protected String selectKeysByClosedRangeTemplate;

    public ComparableFieldQueryCache(EntityDefinition mainDefinition, EntityDefinition fieldEntityDefinition,
            PrimitiveDefinition fieldDefinition, MySqlQueryBuilder queryBuilder) {
        super(mainDefinition, fieldEntityDefinition, fieldDefinition, queryBuilder);
    }

    public String getSelectFullByOpenRangeQuery(boolean left, boolean strict) {
        if (selectFullByOpenRangeTemplate == null) {
            selectFullByOpenRangeTemplate = buildSelectByOpenRangeTemplate(true);
        }
        return String.format(selectFullByOpenRangeTemplate, buildOperator(left, strict));
    }

    public String getSelectKeysByOpenRangeQuery(boolean left, boolean strict) {
        if (selectKeysByOpenRangeTemplate == null) {
            selectKeysByOpenRangeTemplate = buildSelectByOpenRangeTemplate(false);
        }
        return String.format(selectKeysByOpenRangeTemplate, buildOperator(left, strict));
    }

    public String getSelectFullByClosedRangeQuery(boolean leftStrict, boolean rightStrict) {
        if (selectFullByClosedRangeTemplate == null) {
            selectFullByClosedRangeTemplate = buildSelectByClosedRangeTemplate(true);
        }
        return String.format(selectFullByClosedRangeTemplate,
                buildOperator(true, leftStrict), buildOperator(false, rightStrict));
    }

    public String getSelectKeysByClosedRangeQuery(boolean leftStrict, boolean rightStrict) {
        if (selectKeysByClosedRangeTemplate == null) {
            selectKeysByClosedRangeTemplate = buildSelectByClosedRangeTemplate(false);
        }
        return String.format(selectKeysByClosedRangeTemplate,
                buildOperator(true, leftStrict), buildOperator(false, rightStrict));
    }

    protected String buildSelectByOpenRangeTemplate(boolean full) {
        return queryBuilder.buildSelectEntityBySingleFieldOpenRangeTemplate(mainDefinition, fieldEntityDefinition,
                fieldDefinition, full);
    }

    protected String buildSelectByClosedRangeTemplate(boolean full) {
        return queryBuilder.buildSelectEntityBySingleFieldClosedRangeTemplate(mainDefinition, fieldEntityDefinition,
                fieldDefinition, full);
    }

    protected String buildOperator(boolean left, boolean strict) {
        String operator = left ? GREATER : LESS;
        if (!strict) {
            operator += EQUALS;
        }
        return operator;
    }
}
