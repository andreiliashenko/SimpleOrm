package com.anli.simpleorm.descriptors;

import com.anli.simpleorm.controller.PrimaryKeyGenerator;
import com.anli.simpleorm.queries.EntityQuerySet;
import com.anli.simpleorm.reflective.EntityProcessor;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    public Class getFieldClass(String field) {
        return null;
    }

    public EntityQuerySet getQuerySet() {
        return null;
    }

    public String getFieldBinding(String field) {
        return null;
    }

    public List<String> getFieldBindingsWithParent(String field) {
        return null;
    }

    public Set<String> getSingleFields() {
        return null;
    }

    public Class getFieldElementClass(String field) {
        return null;
    }

    public List<EntityDescriptor> getChildrenDescriptors() {
        return null;
    }

    public String getPrimaryKeyBinding() {
        return null;
    }
}
