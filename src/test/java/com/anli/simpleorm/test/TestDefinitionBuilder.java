package com.anli.simpleorm.test;

import com.anli.simpleorm.controller.PrimaryKeyGenerator;
import com.anli.simpleorm.definitions.CollectionFieldDefinition;
import com.anli.simpleorm.definitions.EntityDefinition;
import com.anli.simpleorm.definitions.FieldDefinition;
import com.anli.simpleorm.definitions.ListFieldDefinition;
import com.anli.simpleorm.definitions.ReferenceFieldDefinition;
import com.anli.simpleorm.test.entities.Atomic;
import com.anli.simpleorm.test.entities.ConcreteA;
import com.anli.simpleorm.test.entities.ConcreteB;
import com.anli.simpleorm.test.entities.Root;
import com.anli.simpleorm.test.entities.Super;

public class TestDefinitionBuilder {

    public static EntityDefinition getAtomicDefinition(PrimaryKeyGenerator generator) {
        FieldDefinition idField = new FieldDefinition("id", "atomic_id");
        FieldDefinition nameField = new FieldDefinition("name", "atomic_name");
        EntityDefinition definition = new EntityDefinition(Atomic.class, "Atomic", "atomics",
                generator, idField);
        definition.addSingleField(nameField);
        return definition;
    }

    public static EntityDefinition getConcreteADefinition(PrimaryKeyGenerator generator, boolean withParent) {
        EntityDefinition definition;
        if (withParent) {
            definition = new EntityDefinition(ConcreteA.class, "ConcreteA",
                    "concretes_a", generator, "concrete_a_id");
        } else {
            FieldDefinition idField = new FieldDefinition("id", "concrete_a_id");
            definition = new EntityDefinition(ConcreteA.class, "ConcreteA",
                    "concretes_a", generator, idField);
        }
        FieldDefinition timeField = new FieldDefinition("time", "time_field");
        EntityDefinition atomicDef = getAtomicDefinition(generator);
        ReferenceFieldDefinition atomicReferenceField = new ReferenceFieldDefinition("atomic", "atomic_ref",
                atomicDef, true);
        CollectionFieldDefinition atomicsCollectionField = new CollectionFieldDefinition("atomicSet", "a_ref",
                atomicDef, false);
        definition.addSingleField(timeField);
        definition.addSingleField(atomicReferenceField);
        definition.addCollectionField(atomicsCollectionField);
        return definition;
    }

    public static EntityDefinition getConcreteBDefinition(PrimaryKeyGenerator generator,
            boolean withParent) {
        EntityDefinition definition;
        if (withParent) {
            definition = new EntityDefinition(ConcreteB.class, "ConcreteB",
                "concretes_b", generator, "concrete_b_id");
        } else {
            FieldDefinition idField = new FieldDefinition("id", "concrete_b_id");
            definition = new EntityDefinition(ConcreteB.class, "ConcreteB",
                "concretes_b", generator, idField);
        }
        FieldDefinition nameField = new FieldDefinition("name", "name_field");
        EntityDefinition atomicDef = getAtomicDefinition(generator);
        ReferenceFieldDefinition atomicReferenceField = new ReferenceFieldDefinition("atomic", "atomic_ref",
                atomicDef, false);
        ListFieldDefinition atomicsCollectionField = new ListFieldDefinition("atomicList", "b_ref",
                atomicDef, "b_order", true);
        definition.addSingleField(nameField);
        definition.addSingleField(atomicReferenceField);
        definition.addCollectionField(atomicsCollectionField);
        return definition;
    }

    public static EntityDefinition getSuperDefinition(PrimaryKeyGenerator generator, boolean withParent) {
        EntityDefinition aDefinition = getConcreteADefinition(generator, true);
        EntityDefinition bDefinition = getConcreteBDefinition(generator, true);
        EntityDefinition superDefinition;
        if (withParent) {
            superDefinition = new EntityDefinition(Super.class, "Super", "supers",
                generator, "super_id");
        } else {
            FieldDefinition idField = new FieldDefinition("id", "super_id");
            superDefinition = new EntityDefinition(Super.class, "Super", "supers",
                generator, idField);
        }
        superDefinition.addChildDefinition(aDefinition);
        superDefinition.addChildDefinition(bDefinition);
        FieldDefinition numberField = new FieldDefinition("number", "number_column");
        superDefinition.addSingleField(numberField);
        return superDefinition;
    }

    public static EntityDefinition getRootDefinition(PrimaryKeyGenerator generator) {
        EntityDefinition superDef = getSuperDefinition(generator, true);
        FieldDefinition keyField = new FieldDefinition("id", "root_id");
        EntityDefinition root = new EntityDefinition(Root.class, "Root", "roots",
                generator, keyField);
        root.addChildDefinition(superDef);
        return root;
    }
}
