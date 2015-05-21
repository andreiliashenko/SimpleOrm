package com.anli.simpleorm.queries.mysql;

import com.anli.simpleorm.definitions.CollectionFieldDefinition;
import com.anli.simpleorm.definitions.EntityDefinition;
import com.anli.simpleorm.definitions.FieldDefinition;
import com.anli.simpleorm.definitions.ListFieldDefinition;
import com.anli.simpleorm.queries.QueryBuilder;
import com.anli.simpleorm.queries.QueryDescriptor;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;

public class MySqlQueryBuilder implements QueryBuilder {

    protected static final Pattern MACRO_PATTERN = Pattern.compile("\\$\\{[^\\$]*\\}");
    protected static final String ORDERING_SUBQUERY_ALIAS = "ordering_subquery";
    protected static final String ORDERING_SUBQUERY_MACRO = "${ordering}";
    protected static final String LIST_MACRO = "${list}";
    protected static final String KEY_COLUMN = "key_column";
    protected static final String ORDER_COLUMN = "order_column";
    protected static final String PARENT_JOIN_COLUMN = "parent_join_key";

    @Override
    public QueryDescriptor buildSelectEntityQuery(EntityDefinition definition) {
        StringBuilder query = new StringBuilder();
        Map<String, String> resultBindings = new HashMap<>();
        appendSelectFromClause(query, definition, resultBindings);
        query.append(" ");
        Map<String, Integer> parameterBindings = new HashMap<>();
        appendWherePrimaryKeyClause(query, definition, parameterBindings, 1);
        return new QueryDescriptor(query.toString(), parameterBindings, resultBindings);
    }

    @Override
    public QueryDescriptor buildSelectEntitiesByKeysQuery(EntityDefinition definition) {
        StringBuilder query = new StringBuilder();
        Map<String, String> resultBindings = new HashMap<>();
        Map<String, Integer> parameterBindings = new HashMap<>();
        appendSelectFromClause(query, definition, resultBindings);
        query.append(" ");
        appendWherePrimaryKeyInListMacro(query, definition, parameterBindings, 1);
        return new QueryDescriptor(query.toString(), parameterBindings, resultBindings);
    }

    @Override
    public QueryDescriptor buildSelectExistingKeysQuery(EntityDefinition definition) {
        StringBuilder query = new StringBuilder();
        Map<String, String> resultBindings = new HashMap<>();
        Map<String, Integer> parameterBindings = new HashMap<>();
        appendSelectFromKeysClause(query, definition, resultBindings);
        query.append(" ");
        appendWherePrimaryKeyInListMacro(query, definition, parameterBindings, 1);
        return new QueryDescriptor(query.toString(), parameterBindings, resultBindings);
    }

    @Override
    public List<QueryDescriptor> buildInsertFullEntityQueries(EntityDefinition definition) {
        LinkedList<QueryDescriptor> queryList = new LinkedList<>();
        EntityDefinition currentDefinition = definition;
        EntityDefinition root = definition.getRootDefinition();
        FieldDefinition primaryKey = root.getPrimaryKey();
        String primaryKeyBinding = root.getName() + "." + primaryKey.getName();
        while (currentDefinition != null) {
            StringBuilder query = new StringBuilder();
            Map<String, Integer> parameterBindings = new HashMap<>();
            appendFullInsertQuery(query, currentDefinition, parameterBindings, primaryKeyBinding);
            queryList.addFirst(new QueryDescriptor(query.toString(), parameterBindings,
                    (Map) emptyMap()));
            currentDefinition = currentDefinition.getParentDefinition();
        }
        return queryList;
    }

    @Override
    public List<QueryDescriptor> buildInsertAnemicEntityQueries(EntityDefinition definition) {
        LinkedList<QueryDescriptor> queryList = new LinkedList<>();
        EntityDefinition currentDefinition = definition;
        EntityDefinition root = definition.getRootDefinition();
        FieldDefinition primaryKey = root.getPrimaryKey();
        String primaryKeyBinding = root.getName() + "." + primaryKey.getName();
        while (currentDefinition != null) {
            StringBuilder query = new StringBuilder();
            Map<String, Integer> parameterBindings = new HashMap<>();
            appendFullAnemicInsertClause(query, currentDefinition, parameterBindings, primaryKeyBinding);
            queryList.addFirst(new QueryDescriptor(query.toString(), parameterBindings,
                    (Map) emptyMap()));
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
        appendWherePrimaryKeyClause(query, definition, parameterBindings, lastIndex);
        return new QueryDescriptor(query.toString(), parameterBindings,
                (Map) emptyMap());
    }

    @Override
    public QueryDescriptor buildDeleteEntityQuery(EntityDefinition definition) {
        StringBuilder query = new StringBuilder();
        appendFullDeleteClause(query, definition);
        query.append(" ");
        Map<String, Integer> parameterBindings = new HashMap<>();
        appendWherePrimaryKeyClause(query, definition, parameterBindings, 1);
        return new QueryDescriptor(query.toString(), parameterBindings,
                (Map) emptyMap());
    }

    @Override
    public QueryDescriptor buildSelectCollectionKeysQuery(CollectionFieldDefinition fieldDefinition) {
        StringBuilder query = new StringBuilder();
        Map<String, String> resultBindings = new HashMap<>();
        EntityDefinition referenceDefinition = fieldDefinition.getReferencedDefinition();
        appendSelectFromKeysClause(query, referenceDefinition, resultBindings);
        query.append(" ");
        appendWhereForeignKeyClause(query, fieldDefinition);
        if (fieldDefinition instanceof ListFieldDefinition) {
            query.append(" ");
            appendOrderByClause(query, (ListFieldDefinition) fieldDefinition);
        }
        return new QueryDescriptor(query.toString(),
                singletonMap(FOREIGN_KEY_BINDING, 1), resultBindings);
    }

    @Override
    public QueryDescriptor buildLinkCollectionQuery(CollectionFieldDefinition fieldDefinition) {
        StringBuilder query = new StringBuilder();
        Map<String, Integer> parameterBindings = new HashMap<>();
        if (fieldDefinition instanceof ListFieldDefinition) {
            appendLinkListQueryMacro(query, (ListFieldDefinition) fieldDefinition);
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
    public QueryDescriptor buildClearCollectionQuery(CollectionFieldDefinition fieldDefinition) {
        String query = buildUnlinkCollectionQuery(fieldDefinition, true);
        return new QueryDescriptor(query, singletonMap(FOREIGN_KEY_BINDING, 1),
                (Map) emptyMap());
    }

    @Override
    public QueryDescriptor buildUnlinkCollectionQuery(CollectionFieldDefinition fieldDefinition) {
        String query = buildUnlinkCollectionQuery(fieldDefinition, false);
        Map<String, Integer> parametersBinding = new HashMap<>();
        parametersBinding.put(FOREIGN_KEY_BINDING, 1);
        parametersBinding.put(LINKED_KEYS_BINDING, 2);
        return new QueryDescriptor(query, parametersBinding, (Map) emptyMap());
    }

    protected String buildUnlinkCollectionQuery(CollectionFieldDefinition fieldDefinition, boolean isEmpty) {
        StringBuilder query = new StringBuilder();
        query.append("update ").append(fieldDefinition.getReferencedDefinition().getTable())
                .append(" as ").append(fieldDefinition.getReferencedDefinition().getName().toLowerCase());
        query.append(" ");
        appendSetClearedForeignKeys(query, fieldDefinition);
        if (fieldDefinition instanceof ListFieldDefinition) {
            query.append(", ");
            appendSetClearedOrdering(query, (ListFieldDefinition) fieldDefinition);
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
        EntityDefinition root = definition.getRootDefinition();
        String aliasName = root.getName().toLowerCase();
        String primaryKeyName = root.getPrimaryKey().getName();
        String primaryKeyColumn = root.getPrimaryKey().getColumn();
        String columnAlias = aliasName + "_" + primaryKeyName;
        String columnName = aliasName + "." + primaryKeyColumn;
        resultBindings.put(root.getName() + "." + primaryKeyName, columnAlias);
        query.append("select distinct ");
        query.append(columnName).append(" as ").append(columnAlias);
        query.append(" ");
        appendFullFromClause(query, definition, false);
    }

    protected void appendFieldList(StringBuilder query, EntityDefinition definition,
            Map<String, String> resultBindings, boolean withChildren, boolean withParent) {
        boolean isCommaNeeded = false;
        EntityDefinition parentDefinition = definition.getParentDefinition();
        boolean hasParent = parentDefinition != null;
        if (hasParent && withParent) {
            appendFieldList(query, parentDefinition, resultBindings, false, true);
            isCommaNeeded = true;
        }
        String tableAlias = definition.getName().toLowerCase();
        if (hasParent) {
            if (isCommaNeeded) {
                query.append(", ");
            } else {
                isCommaNeeded = true;
            }
            String fieldAlias = tableAlias + "_" + PARENT_JOIN_COLUMN;
            query.append(tableAlias).append(".").append(definition.getParentJoinColumn())
                    .append(" as ").append(fieldAlias);
            String fieldBinding = definition.getName() + "." + PARENT_JOIN_KEY_BINDING;
            resultBindings.put(fieldBinding, fieldAlias);
        }
        for (FieldDefinition field : definition.getSingleFields()) {
            if (isCommaNeeded) {
                query.append(", ");
            } else {
                isCommaNeeded = true;
            }
            String fieldAlias = tableAlias + "_" + field.getName();
            query.append(tableAlias).append(".").append(field.getColumn())
                    .append(" as ").append(fieldAlias);
            String fieldBinding = definition.getName() + "." + field.getName();
            resultBindings.put(fieldBinding, fieldAlias);
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

    protected void appendFullAnemicInsertClause(StringBuilder query, EntityDefinition definition,
            Map<String, Integer> parameterBindings, String primaryKeyBinding) {
        query.append("insert into ").append(definition.getTable()).append(" (");
        String column = getPrimaryColumn(definition);
        query.append(column);
        query.append(") values (?)");
        parameterBindings.put(primaryKeyBinding, 1);
    }

    protected void appendFullInsertQuery(StringBuilder query, EntityDefinition definition,
            Map<String, Integer> parameterBindings, String primaryKeyBinding) {
        query.append("insert into ").append(definition.getTable()).append(" (");
        int fieldCount = 0;
        if (definition.getParentDefinition() != null) {
            String joinColumn = definition.getParentJoinColumn();
            fieldCount++;
            query.append(joinColumn);
            parameterBindings.put(primaryKeyBinding, fieldCount);
        }
        for (FieldDefinition field : definition.getSingleFields()) {
            if (fieldCount > 0) {
                query.append(", ");
            }
            fieldCount++;
            query.append(field.getColumn());
            parameterBindings.put(definition.getName() + "." + field.getName(), fieldCount);
        }
        query.append(") values (");
        query.append(buildParametersList(fieldCount));
        query.append(")");
    }

    protected void appendWherePrimaryKeyClause(StringBuilder query,
            EntityDefinition definition, Map<String, Integer> parameterBindings, int parameterIndex) {
        EntityDefinition root = definition.getRootDefinition();
        FieldDefinition primaryKey = root.getPrimaryKey();
        query.append("where ").append(root.getName().toLowerCase())
                .append(".").append(primaryKey.getColumn()).append(" = ?");
        parameterBindings.put(root.getName() + "." + primaryKey.getName(), parameterIndex);
    }

    protected void appendFullUpdateClause(StringBuilder query, EntityDefinition definition) {
        query.append("update ").append(definition.getTable()).append(" as ")
                .append(definition.getName().toLowerCase());
        appendJoinClauses(query, definition, false, true);
    }

    protected void appendFullDeleteClause(StringBuilder query, EntityDefinition definition) {
        query.append("delete ");
        appendTableListDelete(query, definition);
        query.append(" ");
        appendFullFromClause(query, definition, false);
    }

    protected void appendTableListDelete(StringBuilder query, EntityDefinition definition) {
        EntityDefinition parent = definition.getParentDefinition();
        if (parent != null) {
            appendTableListDelete(query, parent);
            query.append(", ");
        }
        query.append(definition.getName().toLowerCase());
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
        String joinedColumn = getPrimaryColumn(joinedDefinition);
        appendJoinClause(query, mainDefinition, joinedDefinition, joinedColumn, isLeft);
    }

    protected void appendJoinClause(StringBuilder query, EntityDefinition mainDefinition,
            EntityDefinition joinedDefinition, String joinedColumn, boolean isLeft) {
        if (isLeft) {
            query.append("left ");
        }
        String joinedName = joinedDefinition.getName().toLowerCase();
        String mainColumn = getPrimaryColumn(mainDefinition);
        query.append("join ");
        query.append(joinedDefinition.getTable()).append(" as ").append(joinedName);
        query.append(" on ").append(mainDefinition.getName().toLowerCase()).append(".")
                .append(mainColumn);
        query.append(" = ").append(joinedName)
                .append(".").append(joinedColumn);
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
        for (FieldDefinition field : definition.getSingleFields(false)) {
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

    protected void appendSetOrdering(StringBuilder query, ListFieldDefinition listField) {
        String name = listField.getReferencedDefinition().getName().toLowerCase();
        String orderingColumn = listField.getOrderColumn();
        query.append(name).append(".").append(orderingColumn)
                .append(" = ").append(ORDERING_SUBQUERY_ALIAS).append(".").append(ORDER_COLUMN);
    }

    protected void appendLinkListQueryMacro(StringBuilder query, ListFieldDefinition listField) {
        String table = listField.getReferencedDefinition().getTable();
        String name = listField.getReferencedDefinition().getName().toLowerCase();
        String primaryKeyColumn = listField.getReferencedDefinition().getPrimaryKey().getColumn();
        query.append("update ").append(table).append(" as ").append(name).append(" join (")
                .append(ORDERING_SUBQUERY_MACRO).append(") ").append(ORDERING_SUBQUERY_ALIAS);
        query.append(" on ").append(name).append(".").append(primaryKeyColumn)
                .append(" = ").append(ORDERING_SUBQUERY_ALIAS).append(".").append(KEY_COLUMN);
        query.append(" ");
        appendSetForeignKeys(query, listField);
        query.append(", ");
        appendSetOrdering(query, listField);
    }

    protected void appendLinkCollectionQueryMacro(StringBuilder query, CollectionFieldDefinition field) {
        String table = field.getReferencedDefinition().getTable();
        String name = field.getReferencedDefinition().getName().toLowerCase();
        query.append("update ").append(table).append(" as ").append(name);
        query.append(" ");
        appendSetForeignKeys(query, field);
        query.append(" where ");
        appendKeysInMacro(query, field);
    }

    protected void appendSetForeignKeys(StringBuilder query, CollectionFieldDefinition field) {
        String name = field.getReferencedDefinition().getName().toLowerCase();
        query.append("set ").append(name).append(".")
                .append(field.getForeignKeyColumn()).append(" = ?");
    }

    protected void appendKeysInMacro(StringBuilder query, CollectionFieldDefinition field) {
        String primaryKeyColumn;
        EntityDefinition referencedDefinition = field.getReferencedDefinition();
        if (referencedDefinition.getParentDefinition() != null) {
            primaryKeyColumn = referencedDefinition.getParentJoinColumn();
        } else {
            primaryKeyColumn = referencedDefinition.getPrimaryKey().getColumn();
        }
        String tableAlias = referencedDefinition.getName().toLowerCase();
        query.append(tableAlias).append(".").append(primaryKeyColumn)
                .append(" in (").append(LIST_MACRO).append(")");
    }

    protected void appendSetClearedForeignKeys(StringBuilder query, CollectionFieldDefinition field) {
        String name = field.getReferencedDefinition().getName().toLowerCase();
        String foreignKeyColumn = field.getForeignKeyColumn();
        query.append("set ").append(name).append(".").append(foreignKeyColumn)
                .append(" = null");
    }

    protected void appendSetClearedOrdering(StringBuilder query, ListFieldDefinition listField) {
        String name = listField.getReferencedDefinition().getName().toLowerCase();
        query.append(name).append(".").append(listField.getOrderColumn())
                .append(" = null");
    }

    protected void appendUnlinkCollectionWhereClauseMacro(StringBuilder query, CollectionFieldDefinition field,
            boolean isEmpty) {
        EntityDefinition referencedDefinition = field.getReferencedDefinition();
        String primaryKeyColumn = getPrimaryColumn(referencedDefinition);
        String name = referencedDefinition.getName().toLowerCase();
        query.append("where ").append(name).append(".").append(field.getForeignKeyColumn())
                .append(" = ?");
        if (!isEmpty) {
            query.append(" and ").append(name).append(".").append(primaryKeyColumn)
                    .append(" not in (").append(LIST_MACRO).append(")");
        }
    }

    protected void appendWherePrimaryKeyInListMacro(StringBuilder query, EntityDefinition definition,
            Map<String, Integer> parameterBindings, int parameterIndex) {
        EntityDefinition root = definition.getRootDefinition();
        FieldDefinition primaryKey = root.getPrimaryKey();
        query.append("where ").append(root.getName().toLowerCase()).append(".")
                .append(primaryKey.getColumn()).append(" in (").append(LIST_MACRO).append(")");
        parameterBindings.put(root.getName() + "." + primaryKey.getName(), parameterIndex);
    }

    public QueryDescriptor resolveMacros(QueryDescriptor descriptor,
            Map<String, Object> parameters) {
        TreeMap<Integer, Integer> sizes = new TreeMap<>();
        for (Map.Entry<String, Object> param : parameters.entrySet()) {
            Object value = param.getValue();
            if (value instanceof Collection) {
                int paramBinding = descriptor.getParameterBinding(param.getKey());
                sizes.put(paramBinding, ((Collection) value).size());
            }
        }
        String resolvedQuery = getResolvedQuery(descriptor.getQuery(), sizes.values());
        Map<String, Integer> resolvedParameters = getResolvedIndices(descriptor.getParameterBindings(),
                sizes);
        return new QueryDescriptor(resolvedQuery, resolvedParameters,
                descriptor.getResultBindings());
    }

    protected String getResolvedQuery(String query, Collection<Integer> sizes) {
        Matcher macroMatcher = MACRO_PATTERN.matcher(query);
        StringBuffer buffer = new StringBuffer();
        Iterator<Integer> sizeIter = sizes.iterator();
        while (macroMatcher.find()) {
            String replacement = getMacroReplacement(macroMatcher.group(),
                    sizeIter.next());
            macroMatcher.appendReplacement(buffer, replacement);
        }
        macroMatcher.appendTail(buffer);
        return buffer.toString();
    }

    protected Map<String, Integer> getResolvedIndices(Map<String, Integer> oldIndices,
            Map<Integer, Integer> sizes) {
        Map<String, Integer> resolved = new HashMap<>();
        TreeMap<Integer, String> reversedIndices = new TreeMap<>();
        for (Map.Entry<String, Integer> entry : oldIndices.entrySet()) {
            reversedIndices.put(entry.getValue(), entry.getKey());
        }
        int difference = 0;
        for (Map.Entry<Integer, String> reversed : reversedIndices.entrySet()) {
            int initialIndex = reversed.getKey();
            resolved.put(reversed.getValue(), initialIndex + difference);
            Integer size = sizes.get(initialIndex);
            if (size != null) {
                difference += (size - 1);
            }
        }
        return resolved;
    }

    protected String getMacroReplacement(String macro, int size) {
        if (LIST_MACRO.equals(macro)) {
            return buildParametersList(size);
        }
        if (ORDERING_SUBQUERY_MACRO.equals(macro)) {
            return buildListOrderingSubquery(size);
        }
        throw new RuntimeException("Incorrect macro " + macro);
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

    protected void appendWhereForeignKeyClause(StringBuilder query, CollectionFieldDefinition field) {
        String name = field.getReferencedDefinition().getName().toLowerCase();
        query.append("where ").append(name).append(".").append(field.getForeignKeyColumn())
                .append(" = ?");
    }

    protected void appendOrderByClause(StringBuilder query, ListFieldDefinition listField) {
        String name = listField.getReferencedDefinition().getName().toLowerCase();
        query.append("order by ").append(name).append(".").append(listField.getOrderColumn());
    }

    protected String getPrimaryColumn(EntityDefinition definition) {
        if (definition.getParentDefinition() != null) {
            return definition.getParentJoinColumn();
        }
        return definition.getPrimaryKey().getColumn();
    }
}
