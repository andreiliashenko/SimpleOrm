package com.anli.simpleorm.queries;

import com.anli.simpleorm.definitions.CollectionDefinition;
import com.anli.simpleorm.definitions.EntityDefinition;
import com.anli.simpleorm.definitions.FieldDefinition;
import com.anli.simpleorm.definitions.ListDefinition;
import com.anli.simpleorm.queries.named.NamedQuery;
import java.util.HashMap;
import java.util.Map;

public class MySqlQueryBuilder {

    protected final String ORDERING_SUBQUERY_ALIAS = "ordering_subquery";

    protected void appendSelectFromClause(StringBuilder query, EntityDefinition definition, boolean full) {
        appendSelectClause(query, definition, full);
        query.append(" ");
        appendFullFromClause(query, definition);
    }

    protected void appendSelectClause(StringBuilder query, EntityDefinition definition, boolean full) {
        query.append("select distinct ");
        if (full) {
            appendFieldList(query, definition, true, true);
        } else {
            appendPrimaryKeyField(query, definition);
        }
    }

    protected void appendPrimaryKeyField(StringBuilder query, EntityDefinition definition) {
        query.append(definition.getName().toLowerCase()).append(".").append(definition.getPrimaryKey().getColumn());
    }

    protected void appendFieldList(StringBuilder query, EntityDefinition definition,
            boolean withChildren, boolean withParent) {
        boolean isCommaNeeded = false;
        if (definition.getParentEntity() != null && withParent) {
            appendFieldList(query, definition.getParentEntity(), false, true);
            isCommaNeeded = true;
        }
        String name = definition.getName().toLowerCase();
        for (FieldDefinition field : definition.getSingleFields()) {
            if (isCommaNeeded) {
                query.append(", ");
            } else {
                isCommaNeeded = true;
            }
            StringBuilder element = new StringBuilder();
            element.append(name).append(".").append(field.getColumn());
            query.append(element);
        }
        if (!withChildren) {
            return;
        }

        for (EntityDefinition child : definition.getChildrenEntities()) {
            if (isCommaNeeded) {
                query.append(", ");
            } else {
                isCommaNeeded = true;
            }
            appendFieldList(query, child, true, false);
        }
    }

    protected void appendFullFromClause(StringBuilder query, EntityDefinition definition) {
        query.append("from ").append(definition.getTable()).append(" as ")
                .append(definition.getName().toLowerCase());
        appendJoinClauses(query, definition, true, true);
    }

    protected void appendFullUpdateClause(StringBuilder query, EntityDefinition definition) {
        query.append("update ").append(definition.getTable()).append(" as ")
                .append(definition.getName().toLowerCase());
        appendJoinClauses(query, definition, true, true);
    }

    protected void appendFullDeleteClause(StringBuilder query, EntityDefinition definition) {
        query.append("delete ");
        appendTableListDelete(query, definition, true, true);
        query.append(" ");
        appendFullFromClause(query, definition);
    }

    protected void appendTableListDelete(StringBuilder query, EntityDefinition definition, 
            boolean withChildren, boolean withParent) {
        EntityDefinition parent = definition.getParentEntity();
        if (parent != null && withParent) {
            appendTableListDelete(query, parent, false, true);
            query.append(", ");
        }
        query.append(definition.getName().toLowerCase());
        if (!withChildren) {
            return;
        }
        for (EntityDefinition child : definition.getChildrenEntities()) {
            query.append(", ");
            appendTableListDelete(query, child, true, false);
        }
    }
    
    protected void appendJoinClauses(StringBuilder query, EntityDefinition definition,
            boolean withChildren, boolean withParent) {
        EntityDefinition parentDefinition = definition.getParentEntity();
        if (parentDefinition != null && withParent) {
            query.append(" ");
            appendJoinClause(query, definition, parentDefinition, false);
            appendJoinClauses(query, parentDefinition, false, true);
        }
        if (!withChildren) {
            return;
        }
        for (EntityDefinition childDefinition : definition.getChildrenEntities()) {
            query.append(" ");
            appendJoinClause(query, definition, childDefinition, true);
            appendJoinClauses(query, childDefinition, true, false);
        }
    }

    protected void appendJoinClause(StringBuilder query, EntityDefinition mainDefinition,
            EntityDefinition joinedDefinition, boolean isLeft) {
        if (isLeft) {
            query.append("left ");
        }
        String joinedName = joinedDefinition.getName().toLowerCase();
        query.append("join ");
        query.append(joinedDefinition.getTable()).append(" as ").append(joinedName);
        query.append(" on ").append(mainDefinition.getName().toLowerCase()).append(".")
                .append(mainDefinition.getPrimaryKey().getColumn());
        query.append(" = ").append(joinedName)
                .append(".").append(joinedDefinition.getPrimaryKey().getColumn());
    }

    protected void appendJoinByForeignKeyClause(StringBuilder query, EntityDefinition fieldEntityDefinition, CollectionDefinition fieldDefinition) {
        String joinedTable = fieldDefinition.getReferencedEntity().getTable();
        String joinedAlias = fieldDefinition.getName().toLowerCase()
                + fieldDefinition.getReferencedEntity().getName().toLowerCase();
        query.append("join ").append(joinedTable).append(" as ")
                .append(joinedAlias);
        query.append(" on ").append(fieldEntityDefinition.getName().toLowerCase()).append(".")
                .append(fieldEntityDefinition.getPrimaryKey().getColumn());
        query.append(" = ").append(joinedAlias).append(".").append(fieldDefinition.getForeignKeyColumn());
    }

    protected void appendFullSetClause(StringBuilder query, EntityDefinition definition) {
        query.append("set ");
        appendSetFieldList(query, definition, true, true);
    }

    protected void appendSetFieldList(StringBuilder query, EntityDefinition definition,
            boolean withChildren, boolean withParent) {
        boolean isCommaNeeded = false;
        EntityDefinition parentDefinition = definition.getParentEntity();
        if (parentDefinition != null && withParent) {
            appendSetFieldList(query, parentDefinition, false, true);
            isCommaNeeded = true;
        }
        String name = definition.getName().toLowerCase();
        for (FieldDefinition field : definition.getSingleFields()) {
            if (isCommaNeeded) {
                query.append(", ");
            } else {
                isCommaNeeded = true;
            }
            StringBuilder element = new StringBuilder(name);
            element.append(".").append(field.getColumn()).append(" = ?");
            query.append(element);
        }
        if (!withChildren) {
            return;
        }
        for (EntityDefinition child : definition.getChildrenEntities()) {
            if (isCommaNeeded) {
                query.append(", ");
            } else {
                isCommaNeeded = true;
            }
            appendSetFieldList(query, child, true, false);
        }
    }

    public String buildListOrderingSubquery(ListDefinition listField, int size) {
        String primaryKeyColumn = listField.getReferencedEntity().getPrimaryKey().getColumn();
        String orderingColumn = listField.getOrderColumn();
        StringBuilder subquery = new StringBuilder();
        boolean isUnionNeeded = false;
        for (int i = 0; i < size; i++) {
            if (isUnionNeeded) {
                subquery.append(" union all ");
            } else {
                isUnionNeeded = true;
            }
            subquery.append("select ? as ").append(primaryKeyColumn).append(", ")
                    .append(i).append(" as ").append(orderingColumn).append(" from dual");
        }
        return subquery.toString();
    }

    protected void appendSetForeignKeysAndOrdering(StringBuilder query, ListDefinition listField) {
        String name = listField.getReferencedEntity().getName().toLowerCase();
        String orderingColumn = listField.getOrderColumn();
        query.append("set ").append(name).append(".")
                .append(listField.getForeignKeyColumn()).append(" = ?");
        query.append(", ").append(name).append(".").append(orderingColumn)
                .append(" = ").append(ORDERING_SUBQUERY_ALIAS).append(".").append(orderingColumn);
    }

    protected void appendLinkListQueryTemplate(StringBuilder query, ListDefinition listField) {
        String table = listField.getReferencedEntity().getTable();
        String name = listField.getReferencedEntity().getName().toLowerCase();
        String primaryKeyColumn = listField.getReferencedEntity().getPrimaryKey().getColumn();
        query.append("update ").append(table).append(" as ").append(name).append(" join (")
                .append("%s").append(")").append(ORDERING_SUBQUERY_ALIAS);
        query.append(" on ").append(name).append(".").append(primaryKeyColumn)
                .append(" = ").append(ORDERING_SUBQUERY_ALIAS).append(".").append(primaryKeyColumn);
        query.append(" ");
        appendSetForeignKeysAndOrdering(query, listField);
    }

    protected void appendSetClearedForeignKeysAndOrdering(StringBuilder query, ListDefinition listField) {
        String name = listField.getReferencedEntity().getName().toLowerCase();
        String foreignKeyColumn = listField.getForeignKeyColumn();
        query.append("set ").append(name).append(".").append(foreignKeyColumn)
                .append(" = null, ").append(name).append(".").append(listField.getOrderColumn())
                .append(" = null");
    }

    protected void appendUnlinkCollectionWhereClauseTemplate(StringBuilder query, CollectionDefinition field, boolean isEmpty) {
        String primaryKeyColumn = field.getReferencedEntity().getPrimaryKey().getColumn();
        String name = field.getReferencedEntity().getName().toLowerCase();
        query.append("where ").append(name).append(".").append(field.getForeignKeyColumn())
                .append(" = ?");
        if (!isEmpty) {
            query.append(" and ").append(name).append(".").append(primaryKeyColumn)
                    .append(" not in (%s)");
        }
    }

    public String buildParametersList(int size) {
        StringBuilder list = new StringBuilder();
        boolean isCommaNeeded = false;
        for (int i = 0; i < size; i++) {
            if (isCommaNeeded) {
                list.append(", ");
            } else {
                isCommaNeeded = true;
            }
            list.append("?");
        }
        return list.toString();
    }

    protected void appendSelectListWhereOrderByClause(StringBuilder query, ListDefinition listField) {
        String name = listField.getReferencedEntity().getName().toLowerCase();
        query.append("where ").append(name).append(".").append(listField.getForeignKeyColumn())
                .append(" = ?");
        query.append(" order by ").append(name).append(".").append(listField.getOrderColumn());
    }

    protected void appendWhereEqualsClause(StringBuilder query, EntityDefinition definition,
            FieldDefinition field) {
        String name = (field instanceof CollectionDefinition)
                ? field.getName().toLowerCase() 
                + ((CollectionDefinition) field).getReferencedEntity().getName().toLowerCase()
                : definition.getName().toLowerCase();
        query.append("where ").append(name).append(".").append(field.getColumn())
                .append(" = ?");
    }

    protected void appendWhereStringRegexpClause(StringBuilder query, EntityDefinition definition,
            FieldDefinition field) {
        query.append("where ").append(definition.getName().toLowerCase()).append(".").append(field.getColumn())
                .append(" regexp ?");
    }

    protected void appendWhereInListClauseTemplate(StringBuilder query, EntityDefinition definition,
            FieldDefinition field) {
        String name = (field instanceof CollectionDefinition)
                ? field.getName().toLowerCase()
                + ((CollectionDefinition) field).getReferencedEntity().getName().toLowerCase()
                : definition.getName().toLowerCase();
        query.append("where ").append(name).append(".").append(field.getColumn())
                .append(" in (%s)");
    }

    protected void appendWhereIsNullClause(StringBuilder query, EntityDefinition definition,
            FieldDefinition field) {
        query.append("where ").append(definition.getName().toLowerCase()).append(".").append(field.getColumn())
                .append(" is null");
    }

    protected void appendWhereOpenRangeTemplate(StringBuilder query, EntityDefinition definition,
            FieldDefinition field) {
        query.append("where ").append(definition.getName().toLowerCase()).append(".").append(field.getColumn())
                .append(" %s ?");
    }

    protected void appendWhereClosedRangeTemplate(StringBuilder query, EntityDefinition definition,
            FieldDefinition field) {
        String name = definition.getName().toLowerCase();
        String column = field.getColumn();
        query.append("where ").append(name).append(".").append(column)
                .append(" %s ?");
        query.append(" and ").append(name).append(".").append(column)
                .append(" %s ?");
    }

    protected void appendFullInsertClause(StringBuilder query, EntityDefinition definition) {
        query.append("insert into ").append(definition.getTable()).append("(");
        query.append(definition.getPrimaryKey().getColumn());
        query.append(") values (?)");
    }

    public String buildUpdateEntityQuery(EntityDefinition definition) {
        StringBuilder query = new StringBuilder();
        appendFullUpdateClause(query, definition);
        query.append(" set ");
        appendSetFieldList(query, definition, true, true);
        query.append(" ");
        appendWhereEqualsClause(query, definition, definition.getPrimaryKey());
        return query.toString();
    }

    public String buildDeleteEntityQuery(EntityDefinition definition) {
        StringBuilder query = new StringBuilder();
        appendFullDeleteClause(query, definition);
        query.append(" ");
        appendWhereEqualsClause(query, definition, definition.getPrimaryKey());
        return query.toString();
    }

    public String buildInsertEntityQuery(EntityDefinition definition) {
        StringBuilder query = new StringBuilder();
        appendFullInsertClause(query, definition);
        return query.toString();
    }

    public String buildSelectAllEntities(EntityDefinition definition, boolean full) {
        StringBuilder query = new StringBuilder();
        appendSelectFromClause(query, definition, full);
        return query.toString();
    }

    public String buildSelectEntityBySingleFieldEquals(EntityDefinition definition,
            EntityDefinition fieldEntityDefinition, FieldDefinition fieldDefinition, boolean full) {
        StringBuilder query = new StringBuilder();
        appendSelectFromClause(query, definition, full);
        query.append(" ");
        appendWhereEqualsClause(query, fieldEntityDefinition, fieldDefinition);
        return query.toString();
    }

    public String buildSelectEntityBySingleFieldNull(EntityDefinition definition,
            EntityDefinition fieldEntityDefinition, FieldDefinition fieldDefinition, boolean full) {
        StringBuilder query = new StringBuilder();
        appendSelectFromClause(query, definition, full);
        query.append(" ");
        appendWhereIsNullClause(query, fieldEntityDefinition, fieldDefinition);
        return query.toString();
    }

    public String buildSelectEntityByStringRegexp(EntityDefinition definition,
            EntityDefinition fieldEntityDefinition, FieldDefinition fieldDefinition, boolean full) {
        StringBuilder query = new StringBuilder();
        appendSelectFromClause(query, definition, full);
        query.append(" ");
        appendWhereStringRegexpClause(query, fieldEntityDefinition, fieldDefinition);
        return query.toString();
    }

    public String buildSelectEntityBySingleFieldInListTemplate(EntityDefinition definition,
            EntityDefinition fieldEntityDefinition, FieldDefinition fieldDefinition, boolean full) {
        StringBuilder query = new StringBuilder();
        appendSelectFromClause(query, definition, full);
        query.append(" ");
        appendWhereInListClauseTemplate(query, fieldEntityDefinition, fieldDefinition);
        return query.toString();
    }

    public String buildSelectEntityBySingleFieldOpenRangeTemplate(EntityDefinition definition,
            EntityDefinition fieldEntityDefinition, FieldDefinition fieldDefinition, boolean full) {
        StringBuilder query = new StringBuilder();
        appendSelectFromClause(query, definition, full);
        query.append(" ");
        appendWhereOpenRangeTemplate(query, fieldEntityDefinition, fieldDefinition);
        return query.toString();
    }

    public String buildSelectEntityBySingleFieldClosedRangeTemplate(EntityDefinition definition,
            EntityDefinition fieldEntityDefinition, FieldDefinition fieldDefinition, boolean full) {
        StringBuilder query = new StringBuilder();
        appendSelectFromClause(query, definition, full);
        query.append(" ");
        appendWhereClosedRangeTemplate(query, fieldEntityDefinition, fieldDefinition);
        return query.toString();
    }

    public String buildSelectEntityByCollectionFieldContainsSingle(EntityDefinition definition,
            EntityDefinition fieldEntityDefinition, CollectionDefinition fieldDefinition, boolean full) {
        StringBuilder query = new StringBuilder();
        appendSelectFromClause(query, definition, full);
        query.append(" ");
        appendJoinByForeignKeyClause(query, fieldEntityDefinition, fieldDefinition);
        query.append(" ");
        appendWhereEqualsClause(query, fieldEntityDefinition, fieldDefinition);
        return query.toString();
    }

    public String buildSelectEntityByCollectionFieldContainsListTemplate(EntityDefinition definition,
            EntityDefinition fieldEntityDefinition, CollectionDefinition fieldDefinition, boolean full) {
        StringBuilder query = new StringBuilder();
        appendSelectFromClause(query, definition, full);
        query.append(" ");
        appendJoinByForeignKeyClause(query, fieldEntityDefinition, fieldDefinition);
        query.append(" ");
        appendWhereInListClauseTemplate(query, definition, fieldDefinition);
        return query.toString();
    }

    public String buildSelectCollection(CollectionDefinition fieldDefinition) {
        StringBuilder query = new StringBuilder();
        appendSelectFromClause(query, fieldDefinition.getReferencedEntity(), true);
        query.append(" ");
        if (fieldDefinition instanceof ListDefinition) {
            appendSelectListWhereOrderByClause(query, (ListDefinition) fieldDefinition);
        } else {
            throw new UnsupportedOperationException();
        }

        return query.toString();
    }

    public String buildLinkCollectionQueryTemplate(CollectionDefinition fieldDefinition) {
        StringBuilder query = new StringBuilder();
        if (fieldDefinition instanceof ListDefinition) {
            appendLinkListQueryTemplate(query, (ListDefinition) fieldDefinition);
        } else {
            throw new UnsupportedOperationException();
        }
        return query.toString();
    }

    public String buildUnlinkCollectionTemplate(CollectionDefinition fieldDefinition, boolean isEmpty) {
        StringBuilder query = new StringBuilder();
        query.append("update ").append(fieldDefinition.getReferencedEntity().getTable()).append(" ")
                .append(" as ").append(fieldDefinition.getReferencedEntity().getName().toLowerCase());
        query.append(" ");
        if (fieldDefinition instanceof ListDefinition) {
            appendSetClearedForeignKeysAndOrdering(query, (ListDefinition) fieldDefinition);
        } else {
            throw new UnsupportedOperationException();
        }
        query.append(" ");
        appendUnlinkCollectionWhereClauseTemplate(query, fieldDefinition, isEmpty);
        return query.toString();
    }
    
    public String buildSelectByNamedQuery(EntityDefinition definition, NamedQuery namedQuery, boolean full) {
        StringBuilder query = new StringBuilder();
        appendSelectFromClause(query, definition, full);
        query.append(" ");
        query.append(resolveListMacros(namedQuery.getAdditionalJoins()));
        query.append(" ");
        query.append(resolveListMacros(namedQuery.getCriteria()));
        return query.toString();
    }
 
    protected String resolveListMacros(String clause) {
        return clause.replace(NamedQuery.getListMacro(), "%s");
    }
    
    public Map<String, Integer> getKeysIndices(EntityDefinition definition) {
        Map<String, Integer> keyMap = new HashMap<>();
        int lastIndex = countSingleFields(definition.getParentEntity());
        addKeyIndex(definition, lastIndex, keyMap);
        return keyMap;
    }

    protected int countSingleFields(EntityDefinition definition) {
        if (definition == null) {
            return 0;
        }
        int counter = countSingleFields(definition.getParentEntity());
        return counter + definition.getSingleFields().size();
    }

    protected int addKeyIndex(EntityDefinition definition, int lastIndex,
            Map<String, Integer> keyMap) {
        FieldDefinition primaryKey = definition.getPrimaryKey();
        String entityName = definition.getName();
        for (FieldDefinition field : definition.getSingleFields()) {
            lastIndex++;
            if (field == primaryKey) {
                keyMap.put(entityName, lastIndex);
            }
        }
        for (EntityDefinition child : definition.getChildrenEntities()) {
            lastIndex = addKeyIndex(child, lastIndex, keyMap);
        }
        return lastIndex;
    }
}
