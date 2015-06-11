package com.anli.simpleorm.controller.repository;

import com.anli.simpleorm.controller.EntityRepository;
import com.anli.simpleorm.descriptors.CollectionFieldDescriptor;
import com.anli.simpleorm.descriptors.UnitDescriptorManager;
import com.anli.simpleorm.exceptions.NonExistentEntitiesException;
import com.anli.simpleorm.sql.DataRow;
import com.anli.simpleorm.sql.SqlEngine;
import com.anli.simpleorm.test.TestKeyGenerator;
import com.anli.simpleorm.test.entities.Atomic;
import java.math.BigInteger;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static com.anli.simpleorm.test.TestDescriptorManagerBuilder.getTestManager;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class BasicEntityRepositoryAtomicTest {

    protected TestKeyGenerator keyGenerator;
    protected UnitDescriptorManager descriptorManager;

    @Mock
    protected SqlEngine sqlEngine;
    protected EntityRepository repository;

    @Before
    public void setUp() {
        initMocks(this);
        keyGenerator = new TestKeyGenerator();
        descriptorManager = getTestManager(keyGenerator);
        repository = new BasicEntityRepository(descriptorManager, sqlEngine);
    }

    @Test
    public void testCreate() {
        keyGenerator.setLastKey(BigInteger.ZERO);
        Atomic atomic = repository.create(Atomic.class);
        assertEquals(Atomic.class, atomic.getClass());
        assertEquals(BigInteger.valueOf(1), atomic.getId());
        assertNull(atomic.getName());
        verify(sqlEngine).insertAnemicEntity(BigInteger.valueOf(1), Atomic.class);
    }

    @Test
    public void testSaveValid() {
        Atomic atomic = new Atomic();
        atomic.setId(BigInteger.valueOf(2));
        atomic.setName("TestName");
        when(sqlEngine.exists(BigInteger.valueOf(2), Atomic.class)).thenReturn(true);
        Map<String, Object> expectedParameters = new HashMap<>();
        expectedParameters.put("Atomic.id", BigInteger.valueOf(2));
        expectedParameters.put("Atomic.name", "TestName");
        repository.save(atomic);
        verify(sqlEngine).updateEntity(expectedParameters, Atomic.class);
        verify(sqlEngine, never()).updateCollectionLinkage(any(CollectionFieldDescriptor.class),
                any(Collection.class), any());
    }

    @Test
    public void testSaveNonExistent() {
        Atomic atomic = new Atomic();
        atomic.setId(BigInteger.valueOf(3));
        when(sqlEngine.exists(BigInteger.valueOf(3), Atomic.class)).thenReturn(false);
        try {
            repository.save(atomic);
            fail();
        } catch (NonExistentEntitiesException ex) {
            assertTrue(ex.getEntities().size() == 1);
            assertSame(atomic, ex.getEntities().get(0));
            verify(sqlEngine, never()).updateEntity(any(Map.class), any(Class.class));
        }
    }

    @Test
    public void testReadExistent() {
        DataRow row = new DataRow();
        row.put("Atomic.id", BigInteger.valueOf(4));
        row.put("Atomic.name", "TestName");
        when(sqlEngine.getByPrimaryKey(BigInteger.valueOf(4), Atomic.class)).thenReturn(row);
        Atomic atomic = repository.getByPrimaryKey(BigInteger.valueOf(4), Atomic.class);
        assertEquals(BigInteger.valueOf(4), atomic.getId());
        assertEquals("TestName", atomic.getName());
    }

    @Test
    public void testReadNonExistent() {
        when(sqlEngine.getByPrimaryKey(BigInteger.valueOf(4), Atomic.class)).thenReturn(null);
        Atomic atomic = repository.getByPrimaryKey(BigInteger.valueOf(4), Atomic.class);
        assertNull(atomic);
    }

    @Test
    public void testRemoveExistent() {
        Atomic atomic = new Atomic();
        atomic.setId(BigInteger.valueOf(5));
        when(sqlEngine.exists(BigInteger.valueOf(5), Atomic.class)).thenReturn(true);
        repository.remove(atomic);
        verify(sqlEngine).delete(BigInteger.valueOf(5), Atomic.class);
    }

    @Test
    public void testRemoveNonExistent() {
        Atomic atomic = new Atomic();
        atomic.setId(BigInteger.valueOf(5));
        when(sqlEngine.exists(BigInteger.valueOf(5), Atomic.class)).thenReturn(false);
        try {
            repository.remove(atomic);
            fail();
        } catch (NonExistentEntitiesException ex) {
            assertTrue(ex.getEntities().size() == 1);
            assertSame(atomic, ex.getEntities().get(0));
            verify(sqlEngine, never()).delete(BigInteger.valueOf(5), Atomic.class);
        }
    }
}
