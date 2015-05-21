package com.anli.simpleorm.descriptors;

import java.util.ArrayList;
import java.util.List;

public class FieldDescriptor {

    protected final String name;
    protected final String binding;
    protected final Class fieldClass;
    protected final boolean reference;
    protected final boolean lazy;

    public FieldDescriptor(String name, String binding, Class fieldClass, boolean reference, boolean lazy) {
        this.name = name;
        this.binding = binding;
        this.fieldClass = fieldClass;
        this.reference = reference;
        this.lazy = lazy;
    }

    public FieldDescriptor(String name, String binding, Class fieldClass) {
        this(name, binding, fieldClass, false, false);
    }

    public String getName() {
        return name;
    }

    public String getBinding() {
        return binding;
    }

    public Class getFieldClass() {
        return fieldClass;
    }

    public boolean isLazy() {
        return lazy;
    }

    public boolean isReference() {
        return reference;
    }
}
