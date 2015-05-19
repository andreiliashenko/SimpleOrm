package com.anli.simpleorm.definitions;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class EntityDefinition {

    protected final Class entityClass;
    protected String primaryKeyName;
    protected final String name;
    protected String table;
    protected EntityDefinition parentDefinition;
    protected final List<EntityDefinition> childrenDefinitions;
    protected final Map<String, FieldDefinition> singleFields;
    protected final Map<String, CollectionDefinition> collectionFields;

    public EntityDefinition(Class entityClass, String name) {
        this.entityClass = entityClass;
        this.name = name;
        this.singleFields = new HashMap<>();
        this.collectionFields = new HashMap<>();
        this.childrenDefinitions = new LinkedList<>();
    }

    public Class getEntityClass() {
        return entityClass;
    }

    public void setPrimaryKeyName(String primaryKeyName) {
        this.primaryKeyName = primaryKeyName;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getName() {
        return name;
    }

    public FieldDefinition getPrimaryKey() {
        return singleFields.get(primaryKeyName);
    }

    public Collection<FieldDefinition> getSingleFields() {
        return singleFields.values();
    }

    public FieldDefinition getSingleField(String name) {
        return singleFields.get(name);
    }

    public void addSingleField(FieldDefinition singleField) {
        singleFields.put(singleField.getName(), singleField);
    }

    public Collection<CollectionDefinition> getCollectionFields() {
        return collectionFields.values();
    }

    public CollectionDefinition getCollectionField(String name) {
        return collectionFields.get(name);
    }

    public void addCollectionField(CollectionDefinition collectionField) {
        collectionFields.put(collectionField.getName(), collectionField);
    }

    public String getTable() {
        return table;
    }

    public EntityDefinition getParentDefinition() {
        return parentDefinition;
    }

    public List<EntityDefinition> getChildrenDefinitions() {
        return childrenDefinitions;
    }

    public void addChildrenEntity(EntityDefinition childrenEntity) {
        this.childrenDefinitions.add(childrenEntity);
        childrenEntity.parentDefinition = this;
    }
}
