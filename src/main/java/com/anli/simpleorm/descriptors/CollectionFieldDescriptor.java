package com.anli.simpleorm.descriptors;

import com.anli.simpleorm.queries.CollectionQuerySet;
import java.util.List;

public class CollectionFieldDescriptor extends FieldDescriptor {

    protected final Class elementClass;
    protected final CollectionQuerySet querySet;

    public CollectionFieldDescriptor(String name, String binding, Class fieldClass,
            Class elementClass, CollectionQuerySet querySet, boolean lazy) {
        super(name, binding, fieldClass, true, lazy);
        this.elementClass = elementClass;
        this.querySet = querySet;
    }

    public CollectionFieldDescriptor(String name, String binding, Class fieldClass,
            Class elementClass, CollectionQuerySet querySet) {
        this(name, binding, fieldClass, elementClass, querySet, false);
    }

    public Class getElementClass() {
        return elementClass;
    }

    public CollectionQuerySet getQuerySet() {
        return querySet;
    }
}
