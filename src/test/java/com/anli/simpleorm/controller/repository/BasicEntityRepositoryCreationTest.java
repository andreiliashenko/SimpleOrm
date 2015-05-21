package com.anli.simpleorm.controller.repository;

import com.anli.simpleorm.controller.EntityRepository;
import com.anli.simpleorm.descriptors.UnitDescriptorManager;
import com.anli.simpleorm.sql.DataRow;
import com.anli.simpleorm.test.MockSqlEngine;
import com.anli.simpleorm.test.TestKeyGenerator;
import com.anli.simpleorm.test.entities.Atomic;
import com.anli.simpleorm.test.entities.ConcreteA;
import com.anli.simpleorm.test.entities.ConcreteB;
import com.anli.simpleorm.test.entities.Root;
import com.anli.simpleorm.test.entities.Super;
import java.math.BigInteger;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

import static com.anli.simpleorm.test.TestDescriptorManagerBuilder.getTestManager;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class BasicEntityRepositoryCreationTest {

    protected TestKeyGenerator keyGenerator;
    protected UnitDescriptorManager descriptorManager;
    protected MockSqlEngine sqlEngine;
    protected EntityRepository repository;

    @Before
    public void setUp() {
        keyGenerator = new TestKeyGenerator();
        descriptorManager = getTestManager(keyGenerator);
        sqlEngine = new MockSqlEngine();
        repository = new BasicEntityRepository(descriptorManager, sqlEngine);
    }

    @Test
    public void testCreate_atomic() {
        keyGenerator.setLastKey(BigInteger.ZERO);
        Atomic atomic = repository.create(Atomic.class);
        assertEquals(BigInteger.valueOf(1), atomic.getId());
        assertNull(atomic.getName());
        assertTrue(sqlEngine.getInsertsCount() == 1);
        Map<BigInteger, DataRow> atomicMap = sqlEngine.getAtomicMap();
        assertTrue(atomicMap.size() == 1);
        DataRow atomicRow = atomicMap.get(BigInteger.valueOf(1));
        assertTrue(atomicRow.size() == 1);
        assertEquals(BigInteger.valueOf(1), atomicRow.get("Atomic.id"));
    }

    @Test
    public void testCreate_root() {
        keyGenerator.setLastKey(BigInteger.ZERO);
        Root root = repository.create(Root.class);
        assertEquals(BigInteger.valueOf(1), root.getId());
        assertTrue(sqlEngine.getInsertsCount() == 1);
        Map<BigInteger, DataRow> rootMap = sqlEngine.getRootMap();
        assertTrue(rootMap.size() == 1);
        DataRow rootRow = rootMap.get(BigInteger.valueOf(1));
        assertTrue(rootRow.size() == 1);
        assertEquals(BigInteger.valueOf(1), rootRow.get("Root.id"));
    }

    @Test
    public void testCreate_super() {
        keyGenerator.setLastKey(BigInteger.ZERO);
        Super sup = repository.create(Super.class);
        assertEquals(BigInteger.valueOf(1), sup.getId());
        assertNull(sup.getNumber());
        assertTrue(sqlEngine.getInsertsCount() == 2);
        Map<BigInteger, DataRow> rootMap = sqlEngine.getRootMap();
        assertTrue(rootMap.size() == 1);
        Map<BigInteger, DataRow> superMap = sqlEngine.getSuperMap();
        assertTrue(rootMap.size() == 1);
        DataRow rootRow = rootMap.get(BigInteger.valueOf(1));
        DataRow superRow = superMap.get(BigInteger.valueOf(1));
        assertSame(rootRow, superRow);
        assertTrue(rootRow.size() == 3);
        assertEquals(BigInteger.valueOf(1), rootRow.get("Root.id"));
        assertEquals(BigInteger.valueOf(1), rootRow.get("Super.id"));
        assertEquals(BigInteger.valueOf(1), rootRow.get("Super.parentJoinKey"));
    }

    @Test
    public void testCreate_concretea() {
        keyGenerator.setLastKey(BigInteger.ZERO);
        ConcreteA a = repository.create(ConcreteA.class);
        assertEquals(BigInteger.valueOf(1), a.getId());
        assertNull(a.getNumber());
        assertNull(a.getTime());
        assertNull(a.getAtomic());
        assertNull(a.getAtomicSet());
        assertTrue(sqlEngine.getInsertsCount() == 3);
        Map<BigInteger, DataRow> rootMap = sqlEngine.getRootMap();
        assertTrue(rootMap.size() == 1);
        Map<BigInteger, DataRow> superMap = sqlEngine.getSuperMap();
        assertTrue(rootMap.size() == 1);
        Map<BigInteger, DataRow> aMap = sqlEngine.getConcreteAMap();
        DataRow rootRow = rootMap.get(BigInteger.valueOf(1));
        DataRow superRow = superMap.get(BigInteger.valueOf(1));
        DataRow aRow = aMap.get(BigInteger.valueOf(1));
        assertSame(rootRow, superRow);
        assertSame(superRow, aRow);
        assertTrue(rootRow.size() == 5);
        assertEquals(BigInteger.valueOf(1), rootRow.get("Root.id"));
        assertEquals(BigInteger.valueOf(1), rootRow.get("Super.id"));
        assertEquals(BigInteger.valueOf(1), rootRow.get("Super.parentJoinKey"));
        assertEquals(BigInteger.valueOf(1), rootRow.get("ConcreteA.id"));
        assertEquals(BigInteger.valueOf(1), rootRow.get("ConcreteA.parentJoinKey"));
    }

    @Test
    public void testCreate_concreteb() {
        keyGenerator.setLastKey(BigInteger.ZERO);
        ConcreteB b = repository.create(ConcreteB.class);
        assertEquals(BigInteger.valueOf(1), b.getId());
        assertNull(b.getNumber());
        assertNull(b.getName());
        assertNull(b.getAtomic());
        assertNull(b.getAtomicList());
        assertTrue(sqlEngine.getInsertsCount() == 3);
        Map<BigInteger, DataRow> rootMap = sqlEngine.getRootMap();
        assertTrue(rootMap.size() == 1);
        Map<BigInteger, DataRow> superMap = sqlEngine.getSuperMap();
        assertTrue(rootMap.size() == 1);
        Map<BigInteger, DataRow> bMap = sqlEngine.getConcreteBMap();
        DataRow rootRow = rootMap.get(BigInteger.valueOf(1));
        DataRow superRow = superMap.get(BigInteger.valueOf(1));
        DataRow bRow = bMap.get(BigInteger.valueOf(1));
        assertSame(rootRow, superRow);
        assertSame(superRow, bRow);
        assertTrue(rootRow.size() == 5);
        assertEquals(BigInteger.valueOf(1), rootRow.get("Root.id"));
        assertEquals(BigInteger.valueOf(1), rootRow.get("Super.id"));
        assertEquals(BigInteger.valueOf(1), rootRow.get("Super.parentJoinKey"));
        assertEquals(BigInteger.valueOf(1), rootRow.get("ConcreteB.id"));
        assertEquals(BigInteger.valueOf(1), rootRow.get("ConcreteB.parentJoinKey"));
    }
}
