package com.anli.simpleorm.descriptors;

import com.anli.simpleorm.controller.PrimaryKeyGenerator;
import com.anli.simpleorm.queries.EntityQuerySet;
import com.anli.simpleorm.reflective.EntityProcessor;
import com.google.common.base.Predicate;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Iterables.filter;

public class EntityDescriptor {

    protected static final PrimitiveFilter PRIMITIVE_FILTER = new PrimitiveFilter();
    protected static final ReferenceFilter REFERENCE_FILTER = new ReferenceFilter();
    protected static final SingleFilter SINGLE_FILTER = new SingleFilter();

    protected final Class entityClass;
    protected final List<EntityDescriptor> childrenDescriptors;
    protected final PrimaryKeyGenerator primaryKeyGenerator;
    protected final EntityProcessor processor;
    protected final EntityQuerySet querySet;
    protected final Map<String, FieldDescriptor> fields;

    protected String primaryKeyName;

    public EntityDescriptor(Class entityClass, PrimaryKeyGenerator primaryKeyGenerator,
            EntityProcessor processor, EntityQuerySet querySet) {
        this.entityClass = entityClass;
        this.primaryKeyGenerator = primaryKeyGenerator;
        this.processor = processor;
        this.querySet = querySet;
        this.childrenDescriptors = new LinkedList<>();
        this.fields = new HashMap<>();
    }

    public Class getEntityClass() {
        return entityClass;
    }

    public PrimaryKeyGenerator getPrimaryKeyGenerator() {
        return primaryKeyGenerator;
    }

    public EntityProcessor getProcessor() {
        return processor;
    }

    public Class getPrimaryKeyClass() {
        return getField(primaryKeyName).getFieldClass();
    }

    public EntityQuerySet getQuerySet() {
        return querySet;
    }

    public Collection<FieldDescriptor> getFields() {
        return fields.values();
    }

    public void addPrimaryKey(FieldDescriptor field) {
        primaryKeyName = field.getName();
        addField(field);
    }

    public void addField(FieldDescriptor field) {
        fields.put(field.getName(), field);
    }

    public Iterable<FieldDescriptor> getPrimitiveFields() {
        return filter(fields.values(), PRIMITIVE_FILTER);
    }

    public Iterable<FieldDescriptor> getSingleFields() {
        return filter(fields.values(), SINGLE_FILTER);
    }

    public Iterable<FieldDescriptor> getReferenceFields() {
        return filter(fields.values(), REFERENCE_FILTER);
    }

    public Iterable<CollectionFieldDescriptor> getCollectionFields() {
        return filter(fields.values(), CollectionFieldDescriptor.class);
    }

    public void addChildDescriptor(EntityDescriptor descriptor) {
        childrenDescriptors.add(descriptor);
    }

    public List<EntityDescriptor> getChildrenDescriptors() {
        return childrenDescriptors;
    }

    public FieldDescriptor getField(String field) {
        return fields.get(field);
    }

    public String getPrimaryKeyBinding() {
        return getField(primaryKeyName).getBinding();
    }

    protected static class ReferenceFilter implements Predicate<FieldDescriptor> {

        @Override
        public boolean apply(FieldDescriptor input) {
            return input.isReference();
        }
    }

    protected static class PrimitiveFilter implements Predicate<FieldDescriptor> {

        @Override
        public boolean apply(FieldDescriptor input) {
            return !input.isReference();
        }
    }

    protected static class SingleFilter implements Predicate<FieldDescriptor> {

        @Override
        public boolean apply(FieldDescriptor input) {
            return !(input instanceof CollectionFieldDescriptor);
        }
    }
}
