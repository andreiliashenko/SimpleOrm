package com.anli.simpleorm.controller.repository;

import com.anli.simpleorm.controller.EntityRepository;
import com.anli.simpleorm.descriptors.CollectionFieldDescriptor;
import com.anli.simpleorm.descriptors.UnitDescriptorManager;
import com.anli.simpleorm.exceptions.LazyFieldException;
import com.anli.simpleorm.sql.DataRow;
import com.anli.simpleorm.sql.SqlEngine;
import com.anli.simpleorm.test.TestKeyGenerator;
import com.anli.simpleorm.test.entities.ConcreteA;
import com.anli.simpleorm.test.entities.ConcreteB;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static com.anli.simpleorm.test.TestDescriptorManagerBuilder.getConcreteAProxy;
import static com.anli.simpleorm.test.TestDescriptorManagerBuilder.getTestManager;
import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
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
