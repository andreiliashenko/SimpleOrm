package com.anli.simpleorm.descriptors;

import com.anli.simpleorm.queries.CollectionQuerySet;

public class CollectionFieldDescriptor extends FieldDescriptor {
    public Class getElementClass() {
        return null;
    }
    
    public CollectionQuerySet getQuerySet() {
        return null;
    }
}
