package com.anli.simpleorm.test;

import com.anli.simpleorm.definitions.CollectionDefinition;
import com.anli.simpleorm.definitions.EntityDefinition;
import com.anli.simpleorm.definitions.FieldDefinition;
import com.anli.simpleorm.definitions.ListDefinition;
import com.anli.simpleorm.definitions.ReferenceDefinition;

public class TestDefinitionBuilder {

    public static EntityDefinition getAtomicDefinition() {
        EntityDefinition definition = new EntityDefinition(null, "Atomic");
        definition.setTable("atomics");
        FieldDefinition idField = new FieldDefinition("id", "atomic_id");
        FieldDefinition nameField = new FieldDefinition("name", "atomic_name");
        definition.addSingleField(nameField);
        definition.addSingleField(idField);
        definition.setPrimaryKeyName("id");
        return definition;
    }

    public static EntityDefinition getConcreteADefinition() {
        EntityDefinition definition = new EntityDefinition(null, "ConcreteA");
        definition.setTable("concretes_a");
        FieldDefinition idField = new FieldDefinition("id", "concrete_a_id");
        FieldDefinition timeField = new FieldDefinition("time", "time_field");
        EntityDefinition atomicDef = getAtomicDefinition();
        ReferenceDefinition atomicReferenceField = new ReferenceDefinition("atomic", "atomic_ref",
                atomicDef, false);
        CollectionDefinition atomicsCollectionField = new CollectionDefinition("atomicSet", "a_ref",
                atomicDef, false);
        definition.addSingleField(idField);
        definition.addSingleField(timeField);
        definition.addSingleField(atomicReferenceField);
        definition.addCollectionField(atomicsCollectionField);
        definition.setPrimaryKeyName("id");
        return definition;
    }

    public static EntityDefinition getConcreteBDefinition() {
        EntityDefinition definition = new EntityDefinition(null, "ConcreteB");
        definition.setTable("concretes_b");
        FieldDefinition idField = new FieldDefinition("id", "concrete_b_id");
        FieldDefinition nameField = new FieldDefinition("name", "name_field");
        EntityDefinition atomicDef = getAtomicDefinition();
        ReferenceDefinition atomicReferenceField = new ReferenceDefinition("atomic", "atomic_ref",
                atomicDef, false);
        ListDefinition atomicsCollectionField = new ListDefinition("atomicList", "b_ref",
                atomicDef, "b_order", false);
        definition.addSingleField(idField);
        definition.addSingleField(nameField);
        definition.addSingleField(atomicReferenceField);
        definition.addCollectionField(atomicsCollectionField);
        definition.setPrimaryKeyName("id");
        return definition;
    }

    public static EntityDefinition getSuperDefinition() {
        EntityDefinition aDefinition = getConcreteADefinition();
        EntityDefinition bDefinition = getConcreteBDefinition();
        EntityDefinition superDefinition = new EntityDefinition(null, "Super");
        superDefinition.addChildrenEntity(aDefinition);
        superDefinition.addChildrenEntity(bDefinition);
        FieldDefinition keyField = new FieldDefinition("id", "super_id");
        FieldDefinition numberField = new FieldDefinition("number", "number_column");
        superDefinition.addSingleField(numberField);
        superDefinition.addSingleField(keyField);
        superDefinition.setTable("supers");
        superDefinition.setPrimaryKeyName("id");
        return superDefinition;
    }

    public static EntityDefinition getRootDefinition() {
        EntityDefinition superDef = getSuperDefinition();
        EntityDefinition root = new EntityDefinition(null, "Root");
        root.addChildrenEntity(superDef);
        FieldDefinition keyField = new FieldDefinition("id", "root_id");
        root.addSingleField(keyField);
        root.setTable("roots");
        root.setPrimaryKeyName("id");
        return root;
    }
}
