package com.anli.simpleorm.descriptors;

import com.anli.simpleorm.controller.PrimaryKeyGenerator;
import com.anli.simpleorm.queries.EntityQuerySet;
import com.anli.simpleorm.reflective.EntityProcessor;
import java.util.Collection;
import java.util.List;

public class EntityDescriptor {

    public PrimaryKeyGenerator getPrimaryKeyGenerator() {
        return null;
    }

    public EntityProcessor getProcessor() {
        return null;
    }

    public Class getPrimaryKeyClass() {
        return null;
    }

    public EntityQuerySet getQuerySet() {
        return null;
    }

    public Collection<FieldDescriptor> getFields() {
        return null;
    }

    public Iterable<FieldDescriptor> getPrimitiveFields() {
        return null;
    }

    public Iterable<FieldDescriptor> getSingleFields() {
        return null;
    }

    public Iterable<FieldDescriptor> getReferenceFields() {
        return null;
    }

    public Iterable<CollectionFieldDescriptor> getCollectionFields() {
        return null;
    }

    public List<EntityDescriptor> getChildrenDescriptors() {
        return null;
    }

    public String getPrimaryKeyBinding() {
        return null;
    }

    public Class getEntityClass() {
        return null;
    }
}
