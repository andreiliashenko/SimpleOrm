package com.anli.simpleorm.definitions;

import com.anli.simpleorm.controller.PrimaryKeyGenerator;
import com.google.common.base.Predicate;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Iterables.filter;

public class EntityDefinition {

    protected final Class entityClass;
    protected final PrimaryKeyGenerator primaryKeyGenerator;
    protected final String name;
    protected final String table;
    protected final String parentJoinColumn;
    protected final String primaryKeyName;

    protected EntityDefinition parentDefinition;
    protected final List<EntityDefinition> childrenDefinitions;
    protected final Map<String, FieldDefinition> singleFields;
    protected final Map<String, CollectionFieldDefinition> collectionFields;

    protected final NotPrimaryKeyPredicate pkFilter;

    public EntityDefinition(Class entityClass, String name, String table,
            PrimaryKeyGenerator primaryKeyGenerator, FieldDefinition primaryKey) {
        this(entityClass, name, table, primaryKeyGenerator,
                primaryKey.getName(), null);
        singleFields.put(primaryKeyName, primaryKey);
    }

    public EntityDefinition(Class entityClass, String name, String table,
            PrimaryKeyGenerator primaryKeyGenerator, String parentJoinColumn) {
        this(entityClass, name, table, primaryKeyGenerator, null, parentJoinColumn);
    }

    protected EntityDefinition(Class entityClass, String name, String table,
            PrimaryKeyGenerator primaryKeyGenerator, String primaryKeyName, String parentJoinColumn) {
        this.entityClass = entityClass;
        this.name = name;
        this.table = table;
        this.primaryKeyGenerator = primaryKeyGenerator;
        this.singleFields = new HashMap<>();
        this.collectionFields = new HashMap<>();
        this.childrenDefinitions = new LinkedList<>();
        this.primaryKeyName = primaryKeyName;
        this.parentJoinColumn = parentJoinColumn;
        this.pkFilter = new NotPrimaryKeyPredicate();
    }

    public Class getEntityClass() {
        return entityClass;
    }

    public String getName() {
        return name;
    }

    public FieldDefinition getPrimaryKey() {
        if (primaryKeyName == null) {
            return null;
        }
        return singleFields.get(primaryKeyName);
    }

    public String getParentJoinColumn() {
        return parentJoinColumn;
    }

    public Iterable<FieldDefinition> getSingleFields() {
        return getSingleFields(true);
    }

    public Iterable<FieldDefinition> getSingleFields(boolean withPrimaryKey) {
        if (withPrimaryKey || primaryKeyName == null) {
            return singleFields.values();
        } else {
            return filter(singleFields.values(), pkFilter);
        }
    }

    public FieldDefinition getSingleField(String name) {
        return singleFields.get(name);
    }

    public void addSingleField(FieldDefinition singleField) {
        singleFields.put(singleField.getName(), singleField);
    }

    public Collection<CollectionFieldDefinition> getCollectionFields() {
        return collectionFields.values();
    }

    public CollectionFieldDefinition getCollectionField(String name) {
        return collectionFields.get(name);
    }

    public void addCollectionField(CollectionFieldDefinition collectionField) {
        collectionFields.put(collectionField.getName(), collectionField);
    }

    public String getTable() {
        return table;
    }

    public EntityDefinition getParentDefinition() {
        return parentDefinition;
    }

    public void setParentDefinition(EntityDefinition parentDefinition) {
        this.parentDefinition = parentDefinition;
    }

    public List<EntityDefinition> getChildrenDefinitions() {
        return childrenDefinitions;
    }

    public void addChildDefinition(EntityDefinition childDefinition) {
        childDefinition.setParentDefinition(this);
        childrenDefinitions.add(childDefinition);
    }

    public EntityDefinition getRootDefinition() {
        EntityDefinition currentDefinition = this;
        EntityDefinition nextDefinition = getParentDefinition();
        while (nextDefinition != null) {
            currentDefinition = nextDefinition;
            nextDefinition = currentDefinition.getParentDefinition();
        }
        return currentDefinition;
    }

    protected class NotPrimaryKeyPredicate implements Predicate<FieldDefinition> {

        @Override
        public boolean apply(FieldDefinition input) {
            return !primaryKeyName.equals(input.getName());
        }
    }
}
