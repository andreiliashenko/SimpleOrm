package com.anli.simpleorm.test;

import com.anli.simpleorm.reflective.EntityProcessor;
import com.anli.simpleorm.reflective.FieldProcessor;
import com.anli.simpleorm.reflective.repository.RepositoryHandlerData;
import com.anli.simpleorm.reflective.repository.RepositoryProcessor;
import com.anli.simpleorm.test.entities.Atomic;
import com.anli.simpleorm.test.entities.ConcreteA;
import com.anli.simpleorm.test.entities.ConcreteB;
import com.anli.simpleorm.test.entities.IAtomic;
import com.anli.simpleorm.test.entities.Root;
import com.anli.simpleorm.test.entities.Super;

public class TestRepositoryProcessorBuilder {

    public static EntityProcessor getAtomicProcessor() throws NoSuchFieldException {
        RepositoryProcessor processor = new RepositoryProcessor(Atomic.class);
        FieldProcessor idProcessor = new FieldProcessor(Atomic.class.getDeclaredField("id"), "id");
        FieldProcessor nameProcessor = new FieldProcessor(Atomic.class.getDeclaredField("name"), "name");
        processor.addPrimaryKeyProcessor(idProcessor);
        processor.addFieldProcessor(nameProcessor);
        return processor;
    }

    public static EntityProcessor getRootProcessor() throws NoSuchFieldException {
        RepositoryProcessor processor = new RepositoryProcessor(Root.class);
        FieldProcessor idProcessor = new FieldProcessor(Root.class.getDeclaredField("id"), "id");
        processor.addPrimaryKeyProcessor(idProcessor);
        return processor;
    }

    public static EntityProcessor getSuperProcessor() throws NoSuchFieldException {
        RepositoryProcessor processor = new RepositoryProcessor(Super.class);
        FieldProcessor idProcessor = new FieldProcessor(Root.class.getDeclaredField("id"), "id");
        FieldProcessor numberProcessor =
                new FieldProcessor(Super.class.getDeclaredField("number"), "number");
        processor.addPrimaryKeyProcessor(idProcessor);
        processor.addFieldProcessor(numberProcessor);
        return processor;
    }

    public static EntityProcessor getConcreteAProcessor(Class proxyClass) throws NoSuchFieldException,
            NoSuchMethodException {
        RepositoryHandlerData handlerData = new RepositoryHandlerData();
        RepositoryProcessor processor = new RepositoryProcessor(ConcreteA.class, proxyClass, handlerData);
        FieldProcessor idProcessor = new FieldProcessor(Root.class.getDeclaredField("id"), "id");
        FieldProcessor numberProcessor =
                new FieldProcessor(Super.class.getDeclaredField("number"), "number");
        FieldProcessor timeProcessor = new FieldProcessor(ConcreteA.class.getDeclaredField("time"), "time");
        FieldProcessor atomicProcessor =
                new FieldProcessor(ConcreteA.class.getDeclaredField("atomic"), "atomic");
        FieldProcessor atomicSetProcessor =
                new FieldProcessor(ConcreteA.class.getDeclaredField("atomicSet"), "atomicSet");
        handlerData.addReferenceProcessorForGetter(ConcreteA.class.getDeclaredMethod("getAtomic"),
                atomicProcessor);
        handlerData.addReferenceProcessorForSetter(ConcreteA.class.getDeclaredMethod("setAtomic",
                IAtomic.class), atomicProcessor);
        processor.addPrimaryKeyProcessor(idProcessor);
        processor.addFieldProcessor(numberProcessor);
        processor.addFieldProcessor(timeProcessor);
        processor.addFieldProcessor(atomicProcessor);
        processor.addFieldProcessor(atomicSetProcessor);
        return processor;
    }

    public static EntityProcessor getConcreteBProcessor(Class proxyClass) throws NoSuchFieldException,
            NoSuchMethodException {
        RepositoryHandlerData handlerData = new RepositoryHandlerData();
        RepositoryProcessor processor = new RepositoryProcessor(ConcreteB.class, proxyClass, handlerData);
        FieldProcessor idProcessor = new FieldProcessor(Root.class.getDeclaredField("id"), "id");
        FieldProcessor numberProcessor =
                new FieldProcessor(Super.class.getDeclaredField("number"), "number");
        FieldProcessor nameProcessor = new FieldProcessor(ConcreteB.class.getDeclaredField("name"), "name");
        FieldProcessor atomicProcessor =
                new FieldProcessor(ConcreteB.class.getDeclaredField("atomic"), "atomic");
        FieldProcessor atomicListProcessor =
                new FieldProcessor(ConcreteB.class.getDeclaredField("atomicList"), "atomicList");
        handlerData.addCollectionProcessorForGetter(ConcreteB.class.getDeclaredMethod("getAtomic"),
                atomicListProcessor);
        processor.addPrimaryKeyProcessor(idProcessor);
        processor.addFieldProcessor(numberProcessor);
        processor.addFieldProcessor(nameProcessor);
        processor.addFieldProcessor(atomicProcessor);
        processor.addFieldProcessor(atomicListProcessor);
        return processor;
    }
}
