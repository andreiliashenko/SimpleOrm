package com.anli.simpleorm.queries;

import com.anli.simpleorm.definitions.CollectionDefinition;
import com.anli.simpleorm.definitions.EntityDefinition;
import com.anli.simpleorm.definitions.ListDefinition;

public class CollectionFieldQueryCache extends FieldQueryCache {

    protected String selectCollection;
    protected String linkCollectionTemplate;
    protected String unlinkNonEmptyCollectionTemplate;
    protected String clearCollection;

    public CollectionFieldQueryCache(EntityDefinition mainDefinition, EntityDefinition fieldEntityDefinition,
            CollectionDefinition fieldDefinition, MySqlQueryBuilder queryBuilder) {
        super(mainDefinition, fieldEntityDefinition, fieldDefinition, queryBuilder);
    }

    public String getSelectCollectionQuery() {
        if (selectCollection == null) {
            selectCollection = buildSelectCollectionQuery();
        }
        return selectCollection;
    }

    public String getLinkCollectionQuery(int size) {
        if (linkCollectionTemplate == null) {
            linkCollectionTemplate = buildLinkCollectionTemplate();
        }
        return String.format(linkCollectionTemplate,
                buildLinkagePart(size));
    }

    public String getUnlinkCollectionQuery(int size) {
        if (size == 0) {
            if (clearCollection == null) {
                clearCollection = buildClearCollectionQuery();
            }
            return clearCollection;
        } else {
            if (unlinkNonEmptyCollectionTemplate == null) {
                unlinkNonEmptyCollectionTemplate = buildUnlinkNonEmptyCollectionTemplate();
            }
            return String.format(unlinkNonEmptyCollectionTemplate,
                    queryBuilder.buildParametersList(size));
        }
    }

    protected String buildSelectCollectionQuery() {
        return queryBuilder.buildSelectCollection((CollectionDefinition) fieldDefinition);
    }

    protected String buildLinkCollectionTemplate() {
        return queryBuilder.buildLinkCollectionQueryTemplate((CollectionDefinition) fieldDefinition);
    }

    protected String buildUnlinkNonEmptyCollectionTemplate() {
        return queryBuilder.buildUnlinkCollectionTemplate((CollectionDefinition) fieldDefinition, false);
    }

    protected String buildClearCollectionQuery() {
        return queryBuilder.buildUnlinkCollectionTemplate((CollectionDefinition) fieldDefinition, true);
    }

    protected String buildLinkagePart(int size) {
        if (fieldDefinition instanceof ListDefinition) {
            return queryBuilder.buildListOrderingSubquery((ListDefinition) fieldDefinition, size);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    protected String buildSelectByEqualsOrContainsQuery(boolean full) {
        return queryBuilder.buildSelectEntityByCollectionFieldContainsSingle(mainDefinition, fieldEntityDefinition,
                (CollectionDefinition) fieldDefinition, full);
    }

    @Override
    protected String buildSelectByAnyTemplate(boolean full) {
        return queryBuilder.buildSelectEntityByCollectionFieldContainsListTemplate(mainDefinition, fieldEntityDefinition,
                (CollectionDefinition) fieldDefinition, full);
    }
}
