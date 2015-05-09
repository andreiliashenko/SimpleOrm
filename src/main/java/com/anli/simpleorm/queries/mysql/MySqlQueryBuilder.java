package com.anli.simpleorm.queries.mysql;

import com.anli.simpleorm.definitions.CollectionDefinition;
import com.anli.simpleorm.definitions.EntityDefinition;
import com.anli.simpleorm.definitions.FieldDefinition;
import com.anli.simpleorm.definitions.ListDefinition;
import com.anli.simpleorm.queries.QueryBuilder;
import com.anli.simpleorm.queries.QueryDescriptor;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MySqlQueryBuilder implements QueryBuilder {

    protected static final String ORDERING_SUBQUERY_ALIAS = "ordering_subquery";
    protected static final String ORDERING_SUBQUERY_MACRO = "${ordering}";
    protected static final String LIST_MACRO = "${list}";
    protected static final String KEY_COLUMN = "key_column";
    protected static final String ORDER_COLUMN = "order_column";

    @Override
    public QueryDescriptor buildSelectEntityQuery(EntityDefinition definition) {
        StringBuilder query = new StringBuilder();
        FieldDefinition primaryKey = definition.getPrimaryKey();
        String primaryKeyName = primaryKey.getName();
        Map<String, Integer> parameterBindings =
                Collections.singletonMap(definition.getName() + "." + primaryKeyName, 1);
        Map<String, String> resultBindings = new HashMap<>();
        appendSelectFromClause(query, definition, resultBindings);
        query.append(" ");
        appendWherePrimaryKeyClause(query, definition);
        return new QueryDescriptor(query.toString(), parameterBindings, resultBindings);
    }

    @Override
    public QueryDescriptor buildSelectEntitiesByKeysQuery(EntityDefinition definition) {
        StringBuilder query = new StringBuilder();
        FieldDefinition primaryKey = definition.getPrimaryKey();
        String primaryKeyName = primaryKey.getName();
        Map<String, Integer> parameterBindings =
                Collections.singletonMap(definition.getName() + "." + primaryKeyName, 1);
        Map<String, String> resultBindings = new HashMap<>();
        appendSelectFromClause(query, definition, resultBindings);
        query.append(" ");
        appendWherePrimaryKeyInListMacro(query, definition);
        return new QueryDescriptor(query.toString(), parameterBindings, resultBindings);
    }

    @Override
    public QueryDescriptor buildSelectExistingKeysQuery(EntityDefinition definition) {
        StringBuilder query = new StringBuilder();
        FieldDefinition primaryKey = definition.getPrimaryKey();
        String primaryKeyName = primaryKey.getName();
        Map<String, Integer> parameterBindings =
                Collections.singletonMap(definition.getName() + "." + primaryKeyName, 1);
        Map<String, String> resultBindings = new HashMap<>();
        appendSelectFromKeysClause(query, definition, resultBindings);
        query.append(" ");
        appendWherePrimaryKeyInListMacro(query, definition);
        return new QueryDescriptor(query.toString(), parameterBindings, resultBindings);
    }

    @Override
    public List<QueryDescriptor> buildInsertFullEntityQueries(EntityDefinition definition) {
        LinkedList<QueryDescriptor> queryList = new LinkedList<>();
        EntityDefinition currentDefinition = definition;
        while (currentDefinition != null) {
            StringBuilder query = new StringBuilder();
            Map<String, Integer> parameterBindings = new HashMap<>();
            int keyBinding = appendFullInsertClause(query, currentDefinition, parameterBindings);
            parameterBindings.put(definition.getName() + "." + definition.getPrimaryKey().getName(),
                    keyBinding);
            queryList.addFirst(new QueryDescriptor(query.toString(), parameterBindings,
                    Collections.<String, String>emptyMap()));
            currentDefinition = currentDefinition.getParentDefinition();
        }
        return queryList;
    }

    @Override
    public List<QueryDescriptor> buildInsertAnemicEntityQueries(EntityDefinition definition) {
        LinkedList<QueryDescriptor> queryList = new LinkedList<>();
        EntityDefinition currentDefinition = definition;
        String primaryKeyName = definition.getPrimaryKey().getName();
        while (currentDefinition != null) {
            StringBuilder query = new StringBuilder();

            Map<String, Integer> parameterBindings =
                    Collections.singletonMap(definition.getName() + "." + primaryKeyName, 1);
            appendFullAnemicInsertClause(query, currentDefinition);
            queryList.addFirst(new QueryDescriptor(query.toString(), parameterBindings,
                    Collections.<String, String>emptyMap()));
            currentDefinition = currentDefinition.getParentDefinition();
        }
        return queryList;
    }

    @Override
    public QueryDescriptor buildUpdateEntityQuery(EntityDefinition definition) {
        StringBuilder query = new StringBuilder();
        Map<String, Integer> parameterBindings = new HashMap<>();
        appendFullUpdateClause(query, definition);
        query.append(" set ");
        int lastIndex = appendSetFieldList(query, definition, parameterBindings, 0);
        query.append(" ");
        lastIndex++;
        parameterBindings.put(definition.getName() + "."
                + definition.getPrimaryKey().getName(), lastIndex);
        appendWherePrimaryKeyClause(query, definition);
        return new QueryDescriptor(query.toString(), parameterBindings,
                Collections.<String, String>emptyMap());
    }

    @Override
    public QueryDescriptor buildDeleteEntityQuery(EntityDefinition definition) {
        StringBuilder query = new StringBuilder();
        appendFullDeleteClause(query, definition);
        query.append(" ");
        appendWherePrimaryKeyClause(query, definition);
        return new QueryDescriptor(query.toString(),
                Collections.singletonMap(definition.getName() + "."
                        + definition.getPrimaryKey().getName(), 1),
                Collections.<String, String>emptyMap());
    }

    @Override
    public QueryDescriptor buildSelectCollectionKeysQuery(CollectionDefinition fieldDefinition) {
        StringBuilder query = new StringBuilder();
        Map<String, String> resultBindings = new HashMap<>();
        EntityDefinition referenceDefinition = fieldDefinition.getReferencedEntity();
        appendSelectFromKeysClause(query, referenceDefinition, resultBindings);
        query.append(" ");
        appendWhereForeignKeyClause(query, fieldDefinition);
        if (fieldDefinition instanceof ListDefinition) {
            query.append(" ");
            appendOrderByClause(query, (ListDefinition) fieldDefinition);
        }
        return new QueryDescriptor(query.toString(),
                Collections.singletonMap(FOREIGN_KEY_BINDING, 1), resultBindings);
    }

    @Override
    public QueryDescriptor buildLinkCollectionQuery(CollectionDefinition fieldDefinition) {
        StringBuilder query = new StringBuilder();
        Map<String, Integer> parameterBindings = new HashMap<>();
        if (fieldDefinition instanceof ListDefinition) {
            appendLinkListQueryMacro(query, (ListDefinition) fieldDefinition);
            parameterBindings.put(LINKED_KEYS_BINDING, 1);
            parameterBindings.put(FOREIGN_KEY_BINDING, 2);
        } else {
            appendLinkCollectionQueryMacro(query, fieldDefinition);
            parameterBindings.put(FOREIGN_KEY_BINDING, 1);
            parameterBindings.put(LINKED_KEYS_BINDING, 2);
        }
        return new QueryDescriptor(query.toString(), parameterBindings,
                Collections.<String, String>emptyMap());
    }

    @Override
    public QueryDescriptor buildClearCollectionQuery(CollectionDefinition fieldDefinition) {
        String query = buildUnlinkCollectionQuery(fieldDefinition, true);
        return new QueryDescriptor(query, Collections.singletonMap(FOREIGN_KEY_BINDING, 1),
                Collections.<String, String>emptyMap());
    }

    @Override
    public QueryDescriptor buildUnlinkCollectionQuery(CollectionDefinition fieldDefinition) {
        String query = buildUnlinkCollectionQuery(fieldDefinition, false);
        Map<String, Integer> parametersBinding = new HashMap<>();
        parametersBinding.put(FOREIGN_KEY_BINDING, 1);
        parametersBinding.put(LINKED_KEYS_BINDING, 2);
        return new QueryDescriptor(query, parametersBinding,
                Collections.<String, String>emptyMap());
    }

    protected String buildUnlinkCollectionQuery(CollectionDefinition fieldDefinition, boolean isEmpty) {
        StringBuilder query = new StringBuilder();
        query.append("update ").append(fieldDefinition.getReferencedEntity().getTable())
                .append(" as ").append(fieldDefinition.getReferencedEntity().getName().toLowerCase());
        query.append(" ");
        appendSetClearedForeignKeys(query, fieldDefinition);
        if (fieldDefinition instanceof ListDefinition) {
            query.append(", ");
            appendSetClearedOrdering(query, (ListDefinition) fieldDefinition);
        }
        query.append(" ");
        appendUnlinkCollectionWhereClauseMacro(query, fieldDefinition, isEmpty);
        return query.toString();
    }

    protected void appendSelectFromClause(StringBuilder query, EntityDefinition definition,
            Map<String, String> resultBindings) {
        appendSelectClause(query, definition, resultBindings);
        query.append(" ");
        appendFullFromClause(query, definition, true);
    }

    protected void appendSelectClause(StringBuilder query, EntityDefinition definition,
            Map<String, String> resultBindings) {
        query.append("select distinct ");
        appendFieldList(query, definition, resultBindings, true, true);
    }

    protected void appendSelectFromKeysClause(StringBuilder query, EntityDefinition definition,
            Map<String, String> resultBindings) {
        String aliasName = definition.getName().toLowerCase();
        String primaryKeyName = definition.getPrimaryKey().getName();
        String primaryKeyColumn = definition.getPrimaryKey().getColumn();
        String columnAlias = aliasName + "_" + primaryKeyName;
        String columnName = aliasName + "." + primaryKeyColumn;
        resultBindings.put(definition.getName() + "." + primaryKeyName, columnAlias);
        query.append("select distinct ");
        query.append(columnName).append(" as ").append(columnAlias);
        query.append(" ");
        query.append("from ").append(definition.getTable()).append(" as ")
                .append(aliasName);
    }

    protected void appendFieldList(StringBuilder query, EntityDefinition definition,
            Map<String, String> resultBindings, boolean withChildren, boolean withParent) {
        boolean isCommaNeeded = false;
        if (definition.getParentDefinition() != null && withParent) {
            appendFieldList(query, definition.getParentDefinition(), resultBindings, false, true);
            isCommaNeeded = true;
        }
        String aliasName = definition.getName().toLowerCase();
        for (FieldDefinition field : definition.getSingleFields()) {
            if (isCommaNeeded) {
                query.append(", ");
            } else {
                isCommaNeeded = true;
            }
            StringBuilder element = new StringBuilder();
            String fieldAlias = aliasName + "_" + field.getName();
            element.append(aliasName).append(".").append(field.getColumn())
                    .append(" as ").append(fieldAlias);
            String fieldBinding = definition.getName() + "." + field.getName();
            resultBindings.put(fieldBinding, fieldAlias);
            query.append(element);
        }
        if (!withChildren) {
            return;
        }

        for (EntityDefinition child : definition.getChildrenDefinitions()) {
            if (isCommaNeeded) {
                query.append(", ");
            } else {
                isCommaNeeded = true;
            }
            appendFieldList(query, child, resultBindings, true, false);
        }
    }

    protected void appendFullFromClause(StringBuilder query, EntityDefinition definition,
            boolean withChildren) {
        query.append("from ").append(definition.getTable()).append(" as ")
                .append(definition.getName().toLowerCase());
        appendJoinClauses(query, definition, withChildren, true);
    }

    protected void appendFullAnemicInsertClause(StringBuilder query, EntityDefinition definition) {
        query.append("insert into ").append(definition.getTable()).append(" (");
        query.append(definition.getPrimaryKey().getColumn());
        query.append(") values (?)");
    }

    protected int appendFullInsertClause(StringBuilder query, EntityDefinition definition,
            Map<String, Integer> parameterBindings) {
        query.append("insert into ").append(definition.getTable()).append(" (");
        int primaryKeyIndex = 0;
        int fieldCount = 0;
        for (FieldDefinition field : definition.getSingleFields()) {
            if (fieldCount > 0) {
                query.append(", ");
            }
            fieldCount++;
            query.append(field.getColumn());
            if (!field.getName().equals(definition.getPrimaryKey().getName())) {
                parameterBindings.put(definition.getName() + "." + field.getName(), fieldCount);
            } else {
                primaryKeyIndex = fieldCount;
            }
        }
        query.append(") values (");
        query.append(buildParametersList(fieldCount));
        query.append(")");
        return primaryKeyIndex;
    }

    protected void appendWherePrimaryKeyClause(StringBuilder query, EntityDefinition definition) {
        query.append("where ").append(definition.getName().toLowerCase())
                .append(".").append(definition.getPrimaryKey().getColumn()).append(" = ?");
    }

    protected void appendFullUpdateClause(StringBuilder query, EntityDefinition definition) {
        query.append("update ").append(definition.getTable()).append(" as ")
                .append(definition.getName().toLowerCase());
        appendJoinClauses(query, definition, false, true);
    }

    protected void appendFullDeleteClause(StringBuilder query, EntityDefinition definition) {
        query.append("delete ");
        appendTableListDelete(query, definition, false, true);
        query.append(" ");
        appendFullFromClause(query, definition, false);
    }

    protected void appendTableListDelete(StringBuilder query, EntityDefinition definition,
            boolean withChildren, boolean withParent) {
        EntityDefinition parent = definition.getParentDefinition();
        if (parent != null && withParent) {
            appendTableListDelete(query, parent, false, true);
            query.append(", ");
        }
        query.append(definition.getName().toLowerCase());
        if (!withChildren) {
            return;
        }
        for (EntityDefinition child : definition.getChildrenDefinitions()) {
            query.append(", ");
            appendTableListDelete(query, child, true, false);
        }
    }

    protected void appendJoinClauses(StringBuilder query, EntityDefinition definition,
            boolean withChildren, boolean withParent) {
        EntityDefinition parentDefinition = definition.getParentDefinition();
        if (parentDefinition != null && withParent) {
            query.append(" ");
            appendJoinClause(query, definition, parentDefinition, false);
            appendJoinClauses(query, parentDefinition, false, true);
        }
        if (!withChildren) {
            return;
        }
        for (EntityDefinition childDefinition : definition.getChildrenDefinitions()) {
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

    protected void appendJoinByForeignKeyClause(StringBuilder query, EntityDefinition fieldEntityDefinition,
            CollectionDefinition fieldDefinition) {
        String joinedTable = fieldDefinition.getReferencedEntity().getTable();
        String joinedAlias = fieldDefinition.getName().toLowerCase()
                + fieldDefinition.getReferencedEntity().getName().toLowerCase();
        query.append("join ").append(joinedTable).append(" as ")
                .append(joinedAlias);
        query.append(" on ").append(fieldEntityDefinition.getName().toLowerCase()).append(".")
                .append(fieldEntityDefinition.getPrimaryKey().getColumn());
        query.append(" = ").append(joinedAlias).append(".").append(fieldDefinition.getForeignKeyColumn());
    }

    protected int appendSetFieldList(StringBuilder query, EntityDefinition definition,
            Map<String, Integer> parameterBindings, int lastIndex) {
        boolean isCommaNeeded = false;
        EntityDefinition parentDefinition = definition.getParentDefinition();
        if (parentDefinition != null) {
            lastIndex = appendSetFieldList(query, parentDefinition, parameterBindings,
                    lastIndex);
            isCommaNeeded = lastIndex > 0;
        }
        String name = definition.getName().toLowerCase();
        String primaryKeyName = definition.getPrimaryKey().getName();
        for (FieldDefinition field : definition.getSingleFields()) {
            if (primaryKeyName.equals(field.getName())) {
                continue;
            }
            if (isCommaNeeded) {
                query.append(", ");
            } else {
                isCommaNeeded = true;
            }
            lastIndex++;
            StringBuilder element = new StringBuilder(name);
            element.append(".").append(field.getColumn()).append(" = ?");
            query.append(element);
            parameterBindings.put(definition.getName() + "." + field.getName(), lastIndex);
        }
        return lastIndex;
    }

    public String buildListOrderingSubquery(int size) {
        StringBuilder subquery = new StringBuilder();
        boolean isUnionNeeded = false;
        for (int i = 0; i < size; i++) {
            if (isUnionNeeded) {
                subquery.append(" union all ");
            } else {
                isUnionNeeded = true;
            }
            subquery.append("select ? as ").append(KEY_COLUMN).append(", ")
                    .append(i).append(" as ").append(ORDER_COLUMN).append(" from dual");
        }
        return subquery.toString();
    }

    protected void appendSetOrdering(StringBuilder query, ListDefinition listField) {
        String name = listField.getReferencedEntity().getName().toLowerCase();
        String orderingColumn = listField.getOrderColumn();
        query.append(name).append(".").append(orderingColumn)
                .append(" = ").append(ORDERING_SUBQUERY_ALIAS).append(".").append(ORDER_COLUMN);
    }

    protected void appendLinkListQueryMacro(StringBuilder query, ListDefinition listField) {
        String table = listField.getReferencedEntity().getTable();
        String name = listField.getReferencedEntity().getName().toLowerCase();
        String primaryKeyColumn = listField.getReferencedEntity().getPrimaryKey().getColumn();
        query.append("update ").append(table).append(" as ").append(name).append(" join (")
                .append(ORDERING_SUBQUERY_MACRO).append(") ").append(ORDERING_SUBQUERY_ALIAS);
        query.append(" on ").append(name).append(".").append(primaryKeyColumn)
                .append(" = ").append(ORDERING_SUBQUERY_ALIAS).append(".").append(KEY_COLUMN);
        query.append(" ");
        appendSetForeignKeys(query, listField);
        query.append(", ");
        appendSetOrdering(query, listField);
    }

    protected void appendLinkCollectionQueryMacro(StringBuilder query, CollectionDefinition field) {
        String table = field.getReferencedEntity().getTable();
        String name = field.getReferencedEntity().getName().toLowerCase();
        query.append("update ").append(table).append(" as ").append(name);
        query.append(" ");
        appendSetForeignKeys(query, field);
        query.append(" where ");
        appendKeysInMacro(query, field);
    }

    protected void appendSetForeignKeys(StringBuilder query, CollectionDefinition field) {
        String name = field.getReferencedEntity().getName().toLowerCase();
        query.append("set ").append(name).append(".")
                .append(field.getForeignKeyColumn()).append(" = ?");
    }

    protected void appendKeysInMacro(StringBuilder query, CollectionDefinition field) {
        String primaryKeyColumn = field.getReferencedEntity().getPrimaryKey().getColumn();
        String tableAlias = field.getReferencedEntity().getName().toLowerCase();
        query.append(tableAlias).append(".").append(primaryKeyColumn)
                .append(" in (").append(LIST_MACRO).append(")");
    }

    protected void appendSetClearedForeignKeys(StringBuilder query, CollectionDefinition field) {
        String name = field.getReferencedEntity().getName().toLowerCase();
        String foreignKeyColumn = field.getForeignKeyColumn();
        query.append("set ").append(name).append(".").append(foreignKeyColumn)
                .append(" = null");
    }

    protected void appendSetClearedOrdering(StringBuilder query, ListDefinition listField) {
        String name = listField.getReferencedEntity().getName().toLowerCase();
        query.append(name).append(".").append(listField.getOrderColumn())
                .append(" = null");
    }

    protected void appendUnlinkCollectionWhereClauseMacro(StringBuilder query, CollectionDefinition field,
            boolean isEmpty) {
        String primaryKeyColumn = field.getReferencedEntity().getPrimaryKey().getColumn();
        String name = field.getReferencedEntity().getName().toLowerCase();
        query.append("where ").append(name).append(".").append(field.getForeignKeyColumn())
                .append(" = ?");
        if (!isEmpty) {
            query.append(" and ").append(name).append(".").append(primaryKeyColumn)
                    .append(" not in (").append(LIST_MACRO).append(")");
        }
    }

    protected void appendWherePrimaryKeyInListMacro(StringBuilder query, EntityDefinition definition) {
        query.append("where ").append(definition.getName().toLowerCase())
                .append(".").append(definition.getPrimaryKey().getColumn()).append(" in (")
                .append(LIST_MACRO).append(")");
    }

    public QueryDescriptor resolveMacros(QueryDescriptor descriptor, 
            Map<String, Object> parameters) {
        
    }

    protected String buildParametersList(int size) {
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

    protected void appendWhereForeignKeyClause(StringBuilder query, CollectionDefinition field) {
        String name = field.getReferencedEntity().getName().toLowerCase();
        query.append("where ").append(name).append(".").append(field.getForeignKeyColumn())
                .append(" = ?");
    }

    protected void appendOrderByClause(StringBuilder query, ListDefinition listField) {
        String name = listField.getReferencedEntity().getName().toLowerCase();
        query.append("order by ").append(name).append(".").append(listField.getOrderColumn());
    }
}
