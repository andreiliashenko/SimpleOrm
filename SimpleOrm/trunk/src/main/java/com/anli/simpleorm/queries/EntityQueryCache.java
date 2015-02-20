package com.anli.simpleorm.queries;

import com.anli.simpleorm.definitions.CollectionDefinition;
import com.anli.simpleorm.definitions.EntityDefinition;
import com.anli.simpleorm.definitions.FieldDefinition;
import com.anli.simpleorm.definitions.PrimitiveDefinition;
import com.anli.simpleorm.definitions.PrimitiveType;
import com.anli.simpleorm.definitions.ReferenceDefinition;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class EntityQueryCache {

    protected final EntityDefinition definition;
    protected final MySqlQueryBuilder queryBuilder;

    protected List<String> insertQueries;
    protected String deleteQuery;
    protected String updateQuery;

    protected String selectAllQuery;
    protected String selectAllKeysQuery;

    protected Map<String, Integer> keysIndices;

    protected final Map<String, FieldQueryCache> fieldCaches;

    public EntityQueryCache(EntityDefinition definition, MySqlQueryBuilder queryBuilder) {
        this.definition = definition;
        this.queryBuilder = queryBuilder;
        this.fieldCaches = new HashMap<>();
    }

    public List<String> getInsertQueries() {
        if (insertQueries == null) {
            LinkedList<String> tempQueries = new LinkedList<>();
            EntityDefinition currentDefinition = definition;
            while (currentDefinition != null) {
                tempQueries
                        .addFirst(queryBuilder.buildInsertEntityQuery(currentDefinition));
                currentDefinition = currentDefinition.getParentEntity();
            }
            insertQueries = new ArrayList<>(tempQueries);
        }
        return insertQueries;
    }

    public String getDeleteQuery() {
        if (deleteQuery == null) {
            deleteQuery = queryBuilder.buildDeleteEntityQuery(definition);
        }
        return deleteQuery;
    }

    public String getUpdateQuery() {
        if (updateQuery == null) {
            updateQuery = queryBuilder.buildUpdateEntityQuery(definition);
        }
        return updateQuery;
    }

    public String getSelectQuery() {
        return getFieldQueryCache(definition.getPrimaryKey().getName())
                .getSelectFullByEqualsOrContainsQuery();
    }

    public String getSelectAllQuery() {
        if (selectAllQuery == null) {
            selectAllQuery = queryBuilder.buildSelectAllEntities(definition, true);
        }
        return selectAllQuery;
    }

    public String getSelectAllKeysQuery() {
        if (selectAllKeysQuery == null) {
            selectAllKeysQuery = queryBuilder.buildSelectAllEntities(definition, false);
        }
        return selectAllKeysQuery;
    }

    public Map<String, Integer> getKeysIndices() {
        if (keysIndices == null) {
            keysIndices = queryBuilder.getKeysIndices(definition);
        }
        return keysIndices;
    }

    public FieldQueryCache getFieldQueryCache(String fieldName) {
        FieldQueryCache cache = fieldCaches.get(fieldName);
        if (cache == null) {
            EntityDefinition fieldDef = definition.getFieldEntity(fieldName);
            cache = createFieldQueryCache(fieldDef, fieldDef.getField(fieldName));
            fieldCaches.put(fieldName, cache);
        }
        return cache;
    }

    protected FieldQueryCache createFieldQueryCache(EntityDefinition fieldEntityDefinition,
            FieldDefinition fieldDefinition) {
        if (fieldDefinition instanceof CollectionDefinition) {
            return new CollectionFieldQueryCache(definition, fieldEntityDefinition,
                    (CollectionDefinition) fieldDefinition, queryBuilder);
        }
        if (fieldDefinition instanceof PrimitiveDefinition) {
            PrimitiveDefinition primitiveDefinition = (PrimitiveDefinition) fieldDefinition;
            PrimitiveType type = primitiveDefinition.getType();
            if (type.isCharacter()) {
                return new CharacterFieldQueryCache(definition, fieldEntityDefinition,
                        primitiveDefinition, queryBuilder);
            } else if (type.isComparable()) {
                return new ComparableFieldQueryCache(definition, fieldEntityDefinition,
                        primitiveDefinition, queryBuilder);
            }
        }
        return new SingleFieldQueryCache(definition, fieldEntityDefinition, fieldDefinition, queryBuilder);
    }

}
