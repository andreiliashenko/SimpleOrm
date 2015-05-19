package com.anli.simpleorm.test;

import com.anli.simpleorm.definitions.CollectionDefinition;
import com.anli.simpleorm.definitions.EntityDefinition;
import com.anli.simpleorm.definitions.FieldDefinition;
import com.anli.simpleorm.definitions.ListDefinition;
import com.anli.simpleorm.definitions.ReferenceDefinition;
import com.anli.simpleorm.test.entities.Atomic;
import com.anli.simpleorm.test.entities.ConcreteA;
import com.anli.simpleorm.test.entities.ConcreteB;
import com.anli.simpleorm.test.entities.Root;
import com.anli.simpleorm.test.entities.Super;

import static com.anli.simpleorm.test.TestKeyGenerator.GENERATOR;

public class TestDefinitionBuilder {

    public static EntityDefinition getAtomicDefinition() {
        EntityDefinition definition = new EntityDefinition(Atomic.class, "Atomic", GENERATOR);
        definition.setTable("atomics");
        FieldDefinition idField = new FieldDefinition("id", "atomic_id");
        FieldDefinition nameField = new FieldDefinition("name", "atomic_name");
        definition.addSingleField(nameField);
        definition.addSingleField(idField);
        definition.setPrimaryKeyName("id");
        return definition;
    }

    public static EntityDefinition getConcreteADefinition() {
        EntityDefinition definition = new EntityDefinition(ConcreteA.class, "ConcreteA", GENERATOR);
        definition.setTable("concretes_a");
        FieldDefinition idField = new FieldDefinition("id", "concrete_a_id");
        FieldDefinition timeField = new FieldDefinition("time", "time_field");
        EntityDefinition atomicDef = getAtomicDefinition();
        ReferenceDefinition atomicReferenceField = new ReferenceDefinition("atomic", "atomic_ref",
                atomicDef, true);
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
        EntityDefinition definition = new EntityDefinition(ConcreteB.class, "ConcreteB", GENERATOR);
        definition.setTable("concretes_b");
        FieldDefinition idField = new FieldDefinition("id", "concrete_b_id");
        FieldDefinition nameField = new FieldDefinition("name", "name_field");
        EntityDefinition atomicDef = getAtomicDefinition();
        ReferenceDefinition atomicReferenceField = new ReferenceDefinition("atomic", "atomic_ref",
                atomicDef, false);
        ListDefinition atomicsCollectionField = new ListDefinition("atomicList", "b_ref",
                atomicDef, "b_order", true);
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
        EntityDefinition superDefinition = new EntityDefinition(Super.class, "Super", GENERATOR);
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
        EntityDefinition root = new EntityDefinition(Root.class, "Root", GENERATOR);
        root.addChildrenEntity(superDef);
        FieldDefinition keyField = new FieldDefinition("id", "root_id");
        root.addSingleField(keyField);
        root.setTable("roots");
        root.setPrimaryKeyName("id");
        return root;
    }
}
