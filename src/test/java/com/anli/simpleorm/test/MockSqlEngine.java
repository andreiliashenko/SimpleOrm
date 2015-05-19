package com.anli.simpleorm.test;

import com.anli.simpleorm.descriptors.CollectionFieldDescriptor;
import com.anli.simpleorm.descriptors.UnitDescriptorManager;
import com.anli.simpleorm.queries.QueryDescriptor;
import com.anli.simpleorm.sql.DataRow;
import com.anli.simpleorm.sql.SqlEngine;
import com.anli.simpleorm.test.entities.Atomic;
import com.anli.simpleorm.test.entities.ConcreteA;
import com.anli.simpleorm.test.entities.ConcreteB;
import com.anli.simpleorm.test.entities.Root;
import com.anli.simpleorm.test.entities.Super;
import com.anli.sqlexecution.execution.SqlExecutor;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MockSqlEngine extends SqlEngine {

    protected final Map<BigInteger, DataRow> atomicMap = new HashMap<>();
    protected final Map<BigInteger, DataRow> concreteAMap = new HashMap<>();
    protected final Map<BigInteger, DataRow> concreteBMap = new HashMap<>();
    protected final Map<BigInteger, DataRow> superMap = new HashMap<>();
    protected final Map<BigInteger, DataRow> rootMap = new HashMap<>();
    protected final Map<BigInteger, List<BigInteger>> atomicSetKeys = new HashMap<>();
    protected final Map<BigInteger, List<BigInteger>> atomicListKeys = new HashMap<>();

    public MockSqlEngine(UnitDescriptorManager descriptorManager, SqlExecutor executor) {
        super(null, null);
    }

    @Override
    public void delete(Object primaryKey, Class entityClass) {
        if (Atomic.class.equals(entityClass)) {
            deleteAtomic((BigInteger) primaryKey);
        } else if (ConcreteA.class.equals(entityClass)) {
            deleteConcreteA((BigInteger) primaryKey);
        } else if (ConcreteB.class.equals(entityClass)) {
            deleteConcreteB((BigInteger) primaryKey);
        } else if (Super.class.equals(entityClass)) {
            deleteSuper((BigInteger) primaryKey);
        } else if (Root.class.equals(entityClass)) {
            deleteConcreteB((BigInteger) primaryKey);
        } else {
            throw new RuntimeException();
        }
    }

    protected void deleteAtomic(BigInteger primaryKey) {
        for (List<BigInteger> set : atomicSetKeys.values()) {
            set.remove(primaryKey);
        }
        for (List<BigInteger> list : atomicListKeys.values()) {
            list.remove(primaryKey);
        }
        atomicMap.remove(primaryKey);
    }

    protected void deleteConcreteA(BigInteger primaryKey) {
        rootMap.remove(primaryKey);
        superMap.remove(primaryKey);
        concreteAMap.remove(primaryKey);
        atomicSetKeys.remove(primaryKey);
    }

    protected void deleteConcreteB(BigInteger primaryKey) {
        rootMap.remove(primaryKey);
        superMap.remove(primaryKey);
        concreteBMap.remove(primaryKey);
        atomicListKeys.remove(primaryKey);
    }

    protected void deleteSuper(BigInteger primaryKey) {
        rootMap.remove(primaryKey);
        superMap.remove(primaryKey);
    }

    protected void deleteRoot(BigInteger primaryKey) {
        rootMap.remove(primaryKey);
    }

    @Override
    public DataRow getByPrimaryKey(Object primaryKey, Class entityClass) {
        Map<BigInteger, DataRow> map = getMapForSelect(entityClass);
        return map.get((BigInteger) primaryKey);
    }

    protected Map<BigInteger, DataRow> getMapForSelect(Class entityClass) {
        if (Atomic.class.equals(entityClass)) {
            return atomicMap;
        } else if (ConcreteA.class.equals(entityClass)) {
            return concreteAMap;
        } else if (ConcreteB.class.equals(entityClass)) {
            return concreteBMap;
        } else if (Super.class.equals(entityClass)) {
            return superMap;
        } else if (Root.class.equals(entityClass)) {
            return rootMap;
        } else {
            throw new RuntimeException();
        }
    }

    @Override
    public Map<Object, DataRow> getCollectionData(Class elementClass, Collection keys) {
        Map<BigInteger, DataRow> dataMap = getMapForSelect(elementClass);
        Map<Object, DataRow> resultMap = new HashMap<>();
        for (Object key : keys) {
            DataRow row = dataMap.get((BigInteger) key);
            if (row != null) {
                resultMap.put(key, row);
            }
        }
        return resultMap;
    }

    @Override
    public List getCollectionKeys(CollectionFieldDescriptor field, Object foreignKey) {
        return new ArrayList(getCollectionKeysList(field, foreignKey));
    }

    protected List<BigInteger> getCollectionKeysList(CollectionFieldDescriptor field, Object foreignKey) {
        switch (field.getName()) {
            case "atomicSet":
                return atomicSetKeys.get((BigInteger) foreignKey);
            case "atomicList":
                return atomicListKeys.get((BigInteger) foreignKey);
        }
        throw new RuntimeException();
    }
    @Override
    public Set getNonExistentKeys(Collection primaryKeys, Class entityClass) {
        Set<BigInteger> resultSet = new HashSet<>(primaryKeys);
        resultSet.removeAll(getMapForSelect(entityClass).keySet());
        return resultSet;
    }

    @Override
    public <E> void insertAnemicEntity(Object primaryKey, Class<E> entityClass) {
        DataRow row = new DataRow();
        if (Atomic.class.equals(entityClass)) {
            insertAtomicPart((BigInteger) primaryKey, row);
        } else if (ConcreteA.class.equals(entityClass)) {
            insertRootPart((BigInteger) primaryKey, row);
            insertSuperPart((BigInteger) primaryKey, row);
            insertConcreteAPart((BigInteger) primaryKey, row);
        } else if (ConcreteB.class.equals(entityClass)) {
            insertRootPart((BigInteger) primaryKey, row);
            insertSuperPart((BigInteger) primaryKey, row);
            insertConcreteBPart((BigInteger) primaryKey, row);
        } else if (Super.class.equals(entityClass)) {
            insertRootPart((BigInteger) primaryKey, row);
            insertSuperPart((BigInteger) primaryKey, row);
        } else if (Root.class.equals(entityClass)) {
            insertRootPart((BigInteger) primaryKey, row);
        } else {
            throw new RuntimeException();
        }
    }

    protected void insertAtomicPart(BigInteger primaryKey, DataRow row) {
        row.put("Atomic.id", primaryKey);
        atomicMap.put(primaryKey, row);
    }

    protected void insertConcreteAPart(BigInteger primaryKey, DataRow row) {
        row.put("ConcreteA.id", primaryKey);
        concreteAMap.put(primaryKey, row);
    }

    protected void insertConcreteBPart(BigInteger primaryKey, DataRow row) {
        row.put("ConcreteB.id", primaryKey);
        concreteBMap.put(primaryKey, row);
    }

    protected void insertSuperPart(BigInteger primaryKey, DataRow row) {
        row.put("Super.id", primaryKey);
        superMap.put(primaryKey, row);
    }

    protected void insertRootPart(BigInteger primaryKey, DataRow row) {
        row.put("Root.id", primaryKey);
        rootMap.put(primaryKey, row);
    }

    @Override
    public void updateCollectionLinkage(CollectionFieldDescriptor field, Collection keys, Object foreignKey) {
        List<BigInteger> collectionKeys = getCollectionKeysList(field, foreignKey);
        collectionKeys.clear();
        collectionKeys.addAll(keys);
    }

    @Override
    public void updateEntity(Map<String, Object> parameters, Class entityClass) {
        String keyName;
        if (Atomic.class.equals(entityClass)) {
            keyName = "Atomic.id";
        } else if (ConcreteA.class.equals(entityClass)) {
            keyName = "ConcreteA.id";
        } else if (ConcreteB.class.equals(entityClass)) {
            keyName = "ConcreteB.id";
        } else if (Super.class.equals(entityClass)) {
            keyName = "Super.id";
        } else if (Root.class.equals(entityClass)) {
            keyName = "Root.id";
        } else {
            throw new RuntimeException();
        }
        Map<BigInteger, DataRow> map = getMapForSelect(entityClass);
        DataRow row = map.get((BigInteger) parameters.get(keyName));
        for (Map.Entry<String, Object> param : parameters.entrySet()) {
            row.put(param.getKey(), param.getValue());
        }
    }

    @Override
    protected QueryDescriptor resolveQuery(QueryDescriptor sourceDescriptor,
            Map<String, Object> parameters) {
        return null;
    }

    @Override
    protected List resolveParameters(QueryDescriptor query, Map<String, Object> parameters) {
        return null;
    }
}
