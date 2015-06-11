package com.anli.simpleorm.controller.repository;

import com.anli.simpleorm.controller.EntityRepository;
import com.anli.simpleorm.descriptors.CollectionFieldDescriptor;
import com.anli.simpleorm.descriptors.UnitDescriptorManager;
import com.anli.simpleorm.exceptions.LazyFieldException;
import com.anli.simpleorm.lazy.LazyList;
import com.anli.simpleorm.sql.DataRow;
import com.anli.simpleorm.sql.SqlEngine;
import com.anli.simpleorm.test.TestKeyGenerator;
import com.anli.simpleorm.test.entities.Atomic;
import com.anli.simpleorm.test.entities.ConcreteA;
import com.anli.simpleorm.test.entities.ConcreteB;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static com.anli.simpleorm.test.TestDescriptorManagerBuilder.getConcreteAProxy;
import static com.anli.simpleorm.test.TestDescriptorManagerBuilder.getTestManager;
import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class BasicEntityRepositoryProxiedTest {

    protected TestKeyGenerator keyGenerator;
    protected UnitDescriptorManager descriptorManager;
    protected CollectionFieldDescriptor atomicSetDescriptor;
    protected CollectionFieldDescriptor atomicListDescriptor;

    @Mock
    protected SqlEngine sqlEngine;
    protected EntityRepository repository;

    @Before
    public void setUp() {
        initMocks(this);
        keyGenerator = new TestKeyGenerator();
        descriptorManager = getTestManager(keyGenerator);
        atomicSetDescriptor = (CollectionFieldDescriptor) descriptorManager
                .getDescriptor(ConcreteA.class).getField("atomicSet");
        atomicListDescriptor = (CollectionFieldDescriptor) descriptorManager
                .getDescriptor(ConcreteB.class).getField("atomicList");
        repository = new BasicEntityRepository(descriptorManager, sqlEngine);
    }

    @Test
    public void testCreateProxied() {
        keyGenerator.setLastKey(BigInteger.ZERO);
        ConcreteA a = repository.create(ConcreteA.class);
        assertEquals(getConcreteAProxy(), a.getClass());
        assertEquals(BigInteger.valueOf(1), a.getId());
        assertNull(a.getNumber());
        assertNull(a.getAtomic());
        assertNull(a.getAtomicSet());
        assertNull(a.getTime());
        verify(sqlEngine).insertAnemicEntity(BigInteger.valueOf(1), ConcreteA.class);
    }

    @Test
    public void testReadNotNullNorEmptyEager() {
        DataRow row = new DataRow();
        row.put("Root.id", BigInteger.valueOf(2));
        row.put("Super.number", BigDecimal.valueOf(1000));
        row.put("ConcreteB.atomic", BigInteger.valueOf(10));
        row.put("ConcreteB.name", "Test ConcreteB Name");
        DataRow atomicRow1 = new DataRow();
        atomicRow1.put("Atomic.id", BigInteger.valueOf(10));
        atomicRow1.put("Atomic.name", "Test10");
        DataRow atomicRow2 = new DataRow();
        atomicRow2.put("Atomic.id", BigInteger.valueOf(11));
        atomicRow2.put("Atomic.name", "Test11");
        DataRow atomicRow3 = new DataRow();
        atomicRow3.put("Atomic.id", BigInteger.valueOf(12));
        atomicRow3.put("Atomic.name", "Test12");
        List<BigInteger> listKeys = newArrayList(BigInteger.valueOf(10), BigInteger.valueOf(11),
                BigInteger.valueOf(12));
        when(sqlEngine.getByPrimaryKey(BigInteger.valueOf(2), ConcreteB.class)).thenReturn(row);
        when(sqlEngine.getByPrimaryKey(BigInteger.valueOf(10), Atomic.class)).thenReturn(atomicRow1);
        when(sqlEngine.getCollectionKeys(atomicListDescriptor, BigInteger.valueOf(2)))
                .thenReturn(listKeys);
        Map<Object, DataRow> collectionMap = new HashMap<>();
        collectionMap.put(BigInteger.valueOf(11), atomicRow2);
        collectionMap.put(BigInteger.valueOf(12), atomicRow3);
        when(sqlEngine.getCollectionData(eq(Atomic.class), any(Collection.class))).thenReturn(collectionMap);
        ConcreteB b = repository.getByPrimaryKey(BigInteger.valueOf(2), ConcreteB.class);
        assertEquals(BigInteger.valueOf(2), b.getId());
        assertEquals(BigDecimal.valueOf(1000), b.getNumber());
        assertEquals("Test ConcreteB Name", b.getName());
        Atomic atomic10 = b.getAtomic();
        assertEquals(BigInteger.valueOf(10), atomic10.getId());
        assertEquals("Test10", atomic10.getName());
        List<Atomic> atomicList = b.getAtomicList();
        assertFalse(atomicList instanceof LazyList);
        assertTrue(atomicList.size() == 3);
        assertSame(atomic10, atomicList.get(0));
        Atomic atomic11 = atomicList.get(1);
        assertEquals(BigInteger.valueOf(11), atomic11.getId());
        assertEquals("Test11", atomic11.getName());
        Atomic atomic12 = atomicList.get(2);
        assertEquals(BigInteger.valueOf(12), atomic12.getId());
        assertEquals("Test12", atomic12.getName());
    }

    @Test
    public void testReadNullAndEmptyEager() {
        DataRow row = new DataRow();
        row.put("Root.id", BigInteger.valueOf(2));
        row.put("Super.number", BigDecimal.valueOf(1000));
        row.put("ConcreteB.atomic", null);
        row.put("ConcreteB.name", "Test ConcreteB Name");
        when(sqlEngine.getByPrimaryKey(BigInteger.valueOf(2), ConcreteB.class)).thenReturn(row);
        when(sqlEngine.getCollectionKeys(atomicListDescriptor,
                BigInteger.valueOf(2))).thenReturn(newArrayList());
        ConcreteB b = repository.getByPrimaryKey(BigInteger.valueOf(2), ConcreteB.class);
        verify(sqlEngine, never()).getByPrimaryKey(any(), eq(Atomic.class));
        verify(sqlEngine, never()).getCollectionData(eq(Atomic.class), any(Collection.class));
        assertEquals(BigInteger.valueOf(2), b.getId());
        assertEquals(BigDecimal.valueOf(1000), b.getNumber());
        assertEquals("Test ConcreteB Name", b.getName());
        assertNull(b.getAtomic());
        assertTrue(b.getAtomicList().isEmpty());
    }

    @Test
    public void testReadNotNullNorEmptyLazy() {
        DataRow row = new DataRow();
        row.put("Root.id", BigInteger.valueOf(3));
        row.put("Super.number", BigDecimal.valueOf(1000));
        row.put("ConcreteA.atomic", BigInteger.valueOf(10));
        row.put("ConcreteA.time", new DateTime(2011, 3, 2, 1, 1, 3, 3));
        List<BigInteger> listKeys = newArrayList(BigInteger.valueOf(10), BigInteger.valueOf(11),
                BigInteger.valueOf(12));
        when(sqlEngine.getByPrimaryKey(BigInteger.valueOf(2), ConcreteA.class)).thenReturn(row);
        when(sqlEngine.getCollectionKeys(atomicSetDescriptor, BigInteger.valueOf(2)))
                .thenReturn(listKeys);
        ConcreteA a = repository.getByPrimaryKey(BigInteger.valueOf(2), ConcreteA.class);
        assertEquals(BigInteger.valueOf(3), a.getId());
        assertEquals(BigDecimal.valueOf(1000), a.getNumber());
        assertTrue(new DateTime(2011, 3, 2, 1, 1, 3, 3).compareTo(a.getTime()) == 0);
        try {
            a.getAtomic();
            fail();
        } catch (LazyFieldException ex) {

        }
        try {
            a.getAtomicSet();
            fail();
        } catch (LazyFieldException ex) {

        }
    }

    @Test
    public void testReadNullAndEmptyLazy() {
        DataRow row = new DataRow();
        row.put("Root.id", BigInteger.valueOf(3));
        row.put("Super.number", BigDecimal.valueOf(1000));
        row.put("ConcreteA.atomic", null);
        row.put("ConcreteA.time", new DateTime(2011, 3, 2, 1, 1, 3, 3));
        when(sqlEngine.getByPrimaryKey(BigInteger.valueOf(2), ConcreteA.class)).thenReturn(row);
        when(sqlEngine.getCollectionKeys(atomicSetDescriptor, BigInteger.valueOf(2)))
                .thenReturn(newArrayList());
        ConcreteA a = repository.getByPrimaryKey(BigInteger.valueOf(2), ConcreteA.class);
        assertEquals(BigInteger.valueOf(3), a.getId());
        assertEquals(BigDecimal.valueOf(1000), a.getNumber());
        assertTrue(new DateTime(2011, 3, 2, 1, 1, 3, 3).compareTo(a.getTime()) == 0);
        assertNull(a.getAtomic());
        try {
            a.getAtomicSet();
            fail();
        } catch (LazyFieldException ex) {

        }
    }
}
