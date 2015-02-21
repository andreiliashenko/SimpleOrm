package com.anli.simpleorm.definitions;

import com.anli.simpleorm.queries.named.NamedQuery;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class EntityDefinition {

    protected String primaryKeyName;
    protected final String name;
    protected final SortedMap<String, FieldDefinition> singleFields;
    protected final SortedMap<String, CollectionDefinition> collectionFields;
    protected String table;
    protected EntityDefinition parentEntity;
    protected final List<EntityDefinition> childrenEntities;
    protected final Map<String, NamedQuery> namedQueries;

    public EntityDefinition(String name) {
        this.name = name;
        this.singleFields = new TreeMap<>();
        this.collectionFields = new TreeMap<>();
        this.childrenEntities = new LinkedList<>();
        this.namedQueries = new TreeMap<>();
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

    public EntityDefinition getParentEntity() {
        return parentEntity;
    }

    public List<EntityDefinition> getChildrenEntities() {
        return childrenEntities;
    }

    public void addChildrenEntity(EntityDefinition childrenEntity) {
        this.childrenEntities.add(childrenEntity);
        childrenEntity.parentEntity = this;
    }

    public void addNamedQuery(NamedQuery query) {
        this.namedQueries.put(query.getName(), query);
    }

    public NamedQuery getNamedQuery(String queryName) {
        EntityDefinition currentEntity = this;
        while (currentEntity != null) {
            NamedQuery query = namedQueries.get(queryName);
            if (query != null) {
                return query;
            }
            currentEntity = currentEntity.getParentEntity();
        }
        return null;
    }

    public FieldDefinition getField(String fieldName) {
        FieldDefinition field = singleFields.get(fieldName);
        if (field == null) {
            field = collectionFields.get(fieldName);
        }
        return field;
    }

    public EntityDefinition getFieldEntity(String fieldName) {
        if (singleFields.containsKey(fieldName)
                || collectionFields.containsKey(fieldName)) {
            return this;
        }
        return parentEntity != null ? parentEntity.getFieldEntity(fieldName) : null;
    }
}
