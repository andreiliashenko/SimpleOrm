package com.anli.simpleorm.descriptors;

import com.anli.simpleorm.controller.PrimaryKeyGenerator;
import com.anli.simpleorm.queries.EntityQuerySet;
import com.anli.simpleorm.reflective.EntityProcessor;
import java.util.Map;

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

    public Class getDataRowClass(String field) {
        return null;
    }

    public EntityQuerySet getQuerySet() {
        return null;
    }

    public String getFieldBinding(String field) {
        return null;
    }

    public Class getFieldElementClass(String field) {
        return null;
    }

    public Map<String, Class> getHierarchicalBindingClasses() {
        return null;
    }

    public String getPrimaryKeyBinding() {
        return null;
    }
}
