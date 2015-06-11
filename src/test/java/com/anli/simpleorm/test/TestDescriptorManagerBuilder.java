package com.anli.simpleorm.test;

import com.anli.simpleorm.controller.PrimaryKeyGenerator;
import com.anli.simpleorm.test.entities.Atomic;
import com.anli.simpleorm.test.entities.ConcreteB;
import com.anli.simpleorm.test.entities.Root;
import com.anli.simpleorm.test.entities.ConcreteA;
import com.anli.simpleorm.test.entities.Super;
import com.anli.simpleorm.descriptors.CollectionFieldDescriptor;
import com.anli.simpleorm.descriptors.EntityDescriptor;
import com.anli.simpleorm.descriptors.FieldDescriptor;
import com.anli.simpleorm.descriptors.UnitDescriptorManager;
import com.anli.simpleorm.queries.CollectionQuerySet;
import com.anli.simpleorm.queries.EntityQuerySet;
import com.anli.simpleorm.queries.QueryDescriptor;
import com.anli.simpleorm.reflective.ProxyClassFactory;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.joda.time.DateTime;

import static com.anli.simpleorm.test.MockMySqlQueries.CLEAR_ATOMIC_LIST;
import static com.anli.simpleorm.test.MockMySqlQueries.CLEAR_ATOMIC_SET;
import static com.anli.simpleorm.test.MockMySqlQueries.DELETE_ROOT;
import static com.anli.simpleorm.test.MockMySqlQueries.INSERT_ANEMIC_ROOT;
import static com.anli.simpleorm.test.MockMySqlQueries.INSERT_ANEMIC_SUPER;
import static com.anli.simpleorm.test.MockMySqlQueries.LINK_ATOMIC_LIST_MAIN;
import static com.anli.simpleorm.test.MockMySqlQueries.LINK_ATOMIC_SET_MAIN;
import static com.anli.simpleorm.test.MockMySqlQueries.LIST_MACRO;
import static com.anli.simpleorm.test.MockMySqlQueries.ORDERING_MACRO;
import static com.anli.simpleorm.test.MockMySqlQueries.SELECT_ATOMIC_BY_KEYS_MAIN;
import static com.anli.simpleorm.test.MockMySqlQueries.SELECT_ATOMIC_EXISTENT_KEYS_MAIN;
import static com.anli.simpleorm.test.MockMySqlQueries.SELECT_ATOMIC_SET;
import static com.anli.simpleorm.test.MockMySqlQueries.SELECT_ROOT_BY_PRIMARY_KEY;
import static com.anli.simpleorm.test.MockMySqlQueries.SELECT_ROOT_EXISTENT_KEYS_MAIN;
import static com.anli.simpleorm.test.MockMySqlQueries.UNLINK_ATOMIC_LIST_MAIN;
import static com.anli.simpleorm.test.MockMySqlQueries.UNLINK_ATOMIC_SET_MAIN;
import static com.anli.simpleorm.test.MockMySqlQueries.UPDATE_SUPER;
import static com.anli.simpleorm.test.TestRepositoryProcessorBuilder.getAtomicProcessor;
import static com.anli.simpleorm.test.TestRepositoryProcessorBuilder.getConcreteAProcessor;
import static com.anli.simpleorm.test.TestRepositoryProcessorBuilder.getConcreteBProcessor;
import static com.anli.simpleorm.test.TestRepositoryProcessorBuilder.getRootProcessor;
import static com.anli.simpleorm.test.TestRepositoryProcessorBuilder.getSuperProcessor;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;

public class TestDescriptorManagerBuilder {

    protected static final Map<String, Integer> linkageParameterBinding = new HashMap<>();
    protected static final Map<String, Integer> reversedLinkageParameterBinding = new HashMap<>();
    protected static final Class concreteAProxy = new ProxyClassFactory().createProxyClass(ConcreteA.class);
    protected static final Class concreteBProxy = new ProxyClassFactory().createProxyClass(ConcreteB.class);

    static {
        linkageParameterBinding.put("foreignKey", 1);
        linkageParameterBinding.put("linkedKeys", 2);
        reversedLinkageParameterBinding.put("linkedKeys", 1);
        reversedLinkageParameterBinding.put("foreignKey", 2);
    }

    public static Class getConcreteAProxy() {
        return concreteAProxy;
    }

    public static Class getConcreteBProxy() {
        return concreteBProxy;
    }

    protected static CollectionFieldDescriptor getAtomicSetDescriptor() {
        CollectionQuerySet atomicSetQueries = new CollectionQuerySet();
        atomicSetQueries.setClearCollectionQuery(new QueryDescriptor(CLEAR_ATOMIC_SET,
                singletonMap("foreignKey", 1), (Map) emptyMap()));
        atomicSetQueries.setLinkCollectionQuery(new QueryDescriptor(LINK_ATOMIC_SET_MAIN + LIST_MACRO,
                linkageParameterBinding, (Map) emptyMap()));
        atomicSetQueries.setUnlinkCollectionQuery(new QueryDescriptor(UNLINK_ATOMIC_SET_MAIN + LIST_MACRO,
                linkageParameterBinding, (Map) emptyMap()));
        atomicSetQueries.setSelectCollectionKeysQuery(new QueryDescriptor(SELECT_ATOMIC_SET,
                singletonMap("foreignKey", 1), singletonMap("Atomic.id", "atomic_id")));
        return new CollectionFieldDescriptor("atomicSet", "ConcreteA.atomicSet",
                Set.class, Atomic.class, atomicSetQueries, true);
    }

    protected static CollectionFieldDescriptor getAtomicListDescriptor() {
        CollectionQuerySet atomicListQueries = new CollectionQuerySet();
        atomicListQueries.setClearCollectionQuery(new QueryDescriptor(CLEAR_ATOMIC_LIST,
                singletonMap("foreignKey", 1), (Map) emptyMap()));
        atomicListQueries.setLinkCollectionQuery(new QueryDescriptor(LINK_ATOMIC_LIST_MAIN + ORDERING_MACRO,
                reversedLinkageParameterBinding, (Map) emptyMap()));
        atomicListQueries.setUnlinkCollectionQuery(new QueryDescriptor(UNLINK_ATOMIC_LIST_MAIN + LIST_MACRO,
                linkageParameterBinding, (Map) emptyMap()));
        return new CollectionFieldDescriptor("atomicList", "ConcreteB.atomicList",
                List.class, Atomic.class, atomicListQueries);
    }

    protected static EntityDescriptor getConcreteADescriptor(PrimaryKeyGenerator generator)
            throws NoSuchFieldException, NoSuchMethodException {
        FieldDescriptor idField = new FieldDescriptor("id", "Root.id", BigInteger.class);
        FieldDescriptor numberField = new FieldDescriptor("number", "Super.number", BigDecimal.class);
        FieldDescriptor timeField = new FieldDescriptor("time", "ConcreteA.time", DateTime.class);
        FieldDescriptor atomicField = new FieldDescriptor("atomic", "ConcreteA.atomic",
                Atomic.class, true, true);
        FieldDescriptor atomicSet = getAtomicSetDescriptor();
        EntityDescriptor descriptor = new EntityDescriptor(ConcreteA.class, generator,
                getConcreteAProcessor(concreteAProxy), null, "ConcreteA.parentJoinKey");
        descriptor.addPrimaryKey(idField);
        descriptor.addField(numberField);
        descriptor.addField(timeField);
        descriptor.addField(atomicField);
        descriptor.addField(atomicSet);
        return descriptor;
    }

    protected static EntityDescriptor getConcreteBDescriptor(PrimaryKeyGenerator generator)
            throws NoSuchFieldException, NoSuchMethodException {
        FieldDescriptor idField = new FieldDescriptor("id", "Root.id", BigInteger.class);
        FieldDescriptor numberField = new FieldDescriptor("number", "Super.number", BigDecimal.class);
        FieldDescriptor nameField = new FieldDescriptor("name", "ConcreteB.name", String.class);
        FieldDescriptor atomicField = new FieldDescriptor("atomic", "ConcreteB.atomic",
                Atomic.class, true, false);
        FieldDescriptor atomicList = getAtomicListDescriptor();
        EntityDescriptor descriptor = new EntityDescriptor(ConcreteB.class, generator,
                getConcreteBProcessor(concreteBProxy), null, "ConcreteB.parentJoinKey");
        descriptor.addPrimaryKey(idField);
        descriptor.addField(numberField);
        descriptor.addField(nameField);
        descriptor.addField(atomicField);
        descriptor.addField(atomicList);
        return descriptor;
    }

    protected static EntityDescriptor getAtomicDescriptor(PrimaryKeyGenerator generator)
            throws NoSuchFieldException {
        EntityQuerySet querySet = new EntityQuerySet();
        Map<String, String> resultBinding = new HashMap<>();
        resultBinding.put("Atomic.id", "atomic_id");
        resultBinding.put("Atomic.name", "atomic_name");
        querySet.setSelectByKeysQuery(new QueryDescriptor(SELECT_ATOMIC_BY_KEYS_MAIN + LIST_MACRO,
                singletonMap("Atomic.id", 1), resultBinding));
        querySet.setSelectExistingKeysQuery(new QueryDescriptor(SELECT_ATOMIC_EXISTENT_KEYS_MAIN
                + LIST_MACRO, singletonMap("Atomic.id", 1), singletonMap("Atomic.id", "atomic_id")));
        FieldDescriptor idField = new FieldDescriptor("id", "Atomic.id", BigInteger.class);
        FieldDescriptor nameField = new FieldDescriptor("name", "Atomic.name", String.class);
        EntityDescriptor descriptor = new EntityDescriptor(Atomic.class, generator,
                getAtomicProcessor(), querySet);
        descriptor.addPrimaryKey(idField);
        descriptor.addField(nameField);
        return descriptor;
    }

    protected static EntityDescriptor getSuperDescriptor(PrimaryKeyGenerator generator)
            throws NoSuchFieldException, NoSuchMethodException {
        EntityQuerySet querySet = new EntityQuerySet();
        Map<String, Integer> paramBindings = new HashMap<>();
        paramBindings.put("Root.id", 2);
        paramBindings.put("Super.number", 1);
        querySet.setUpdateQuery(new QueryDescriptor(UPDATE_SUPER, paramBindings, (Map) emptyMap()));
        QueryDescriptor insertAnemicRoot = new QueryDescriptor(INSERT_ANEMIC_ROOT,
                singletonMap("Root.id", 1), (Map) emptyMap());
        QueryDescriptor insertAnemicSuper = new QueryDescriptor(INSERT_ANEMIC_SUPER,
                singletonMap("Root.id", 1), (Map) emptyMap());
        querySet.setInsertAnemicQueries(asList(insertAnemicRoot, insertAnemicSuper));
        FieldDescriptor idField = new FieldDescriptor("id", "Root.id", BigInteger.class);
        FieldDescriptor numberField = new FieldDescriptor("number", "Atomic.name", BigDecimal.class);
        EntityDescriptor descriptor = new EntityDescriptor(Super.class, generator,
                getSuperProcessor(), querySet, "Super.parentJoinKey");
        descriptor.addPrimaryKey(idField);
        descriptor.addField(numberField);
        descriptor.addChildDescriptor(getConcreteADescriptor(generator));
        descriptor.addChildDescriptor(getConcreteBDescriptor(generator));
        return descriptor;
    }

    protected static EntityDescriptor getRootDescriptor(PrimaryKeyGenerator generator)
            throws NoSuchFieldException, NoSuchMethodException {
        EntityQuerySet querySet = new EntityQuerySet();
        Map<String, String> resultBindings = new HashMap<>();
        resultBindings.put("Root.id", "root_id");
        resultBindings.put("Super.parentJoinKey", "super_parent_join_key");
        resultBindings.put("Super.number", "super_number");
        resultBindings.put("ConcreteA.parentJoinKey", "concretea_parent_join_key");
        resultBindings.put("ConcreteA.time", "concretea_time");
        resultBindings.put("ConcreteA.atomic", "concretea_atomic");
        resultBindings.put("ConcreteB.parentJoinKey", "concreteb_parent_join_key");
        resultBindings.put("ConcreteB.name", "concreteb_name");
        resultBindings.put("ConcreteB.atomic", "concreteb_atomic");
        querySet.setSelectQuery(new QueryDescriptor(SELECT_ROOT_BY_PRIMARY_KEY,
                singletonMap("Root.id", 1), resultBindings));
        querySet.setSelectExistingKeysQuery(new QueryDescriptor(SELECT_ROOT_EXISTENT_KEYS_MAIN
                + LIST_MACRO, singletonMap("Root.id", 1), singletonMap("Root.id", "root_id")));
        querySet.setDeleteQuery(new QueryDescriptor(DELETE_ROOT, singletonMap("Root.id", 1),
                (Map) emptyMap()));
        FieldDescriptor idField = new FieldDescriptor("id", "Root.id", BigInteger.class);
        EntityDescriptor descriptor = new EntityDescriptor(Root.class, generator,
                getRootProcessor(), querySet);
        descriptor.addPrimaryKey(idField);
        descriptor.addChildDescriptor(getSuperDescriptor(generator));
        return descriptor;
    }

    public static UnitDescriptorManager getTestManager(PrimaryKeyGenerator generator) {
        UnitDescriptorManager manager = new UnitDescriptorManager();
        try {
            EntityDescriptor root = getRootDescriptor(generator);
            manager.addDescriptor(root);
            manager.addDescriptor(root.getChildrenDescriptors().get(0));
            manager.addDescriptor(root.getChildrenDescriptors().get(0).getChildrenDescriptors().get(0));
            manager.addDescriptor(root.getChildrenDescriptors().get(0).getChildrenDescriptors().get(1));
            manager.addDescriptor(getAtomicDescriptor(generator));
        } catch (NoSuchFieldException | NoSuchMethodException ex) {
            throw new RuntimeException(ex);
        }
        manager.addProxy(ConcreteA.class, concreteAProxy);
        manager.addProxy(ConcreteB.class, concreteBProxy);
        return manager;
    }
}
