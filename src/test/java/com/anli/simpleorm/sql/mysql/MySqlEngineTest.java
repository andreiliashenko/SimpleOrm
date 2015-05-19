package com.anli.simpleorm.sql.mysql;

import com.anli.simpleorm.test.entities.Atomic;
import com.anli.simpleorm.descriptors.CollectionFieldDescriptor;
import com.anli.simpleorm.descriptors.UnitDescriptorManager;
import com.anli.simpleorm.queries.mysql.MySqlQueryBuilder;
import com.anli.simpleorm.sql.DataRow;
import com.anli.simpleorm.test.entities.ConcreteA;
import com.anli.simpleorm.test.entities.ConcreteB;
import com.anli.simpleorm.test.MockSqlExecutor;
import com.anli.simpleorm.test.MockSqlExecutor.MockQueryKey;
import com.anli.simpleorm.test.MockSqlExecutor.MockRow;
import com.anli.simpleorm.test.entities.Root;
import com.anli.simpleorm.test.entities.Super;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import static com.anli.simpleorm.test.MockMySqlQueries.CLEAR_ATOMIC_LIST;
import static com.anli.simpleorm.test.MockMySqlQueries.CLEAR_ATOMIC_SET;
import static com.anli.simpleorm.test.MockMySqlQueries.DELETE_ROOT;
import static com.anli.simpleorm.test.MockMySqlQueries.INSERT_ANEMIC_ROOT;
import static com.anli.simpleorm.test.MockMySqlQueries.INSERT_ANEMIC_SUPER;
import static com.anli.simpleorm.test.MockMySqlQueries.LINK_ATOMIC_LIST_MAIN;
import static com.anli.simpleorm.test.MockMySqlQueries.LINK_ATOMIC_SET_MAIN;
import static com.anli.simpleorm.test.MockMySqlQueries.SELECT_ATOMIC_BY_KEYS_MAIN;
import static com.anli.simpleorm.test.MockMySqlQueries.SELECT_ATOMIC_EXISTENT_KEYS_MAIN;
import static com.anli.simpleorm.test.MockMySqlQueries.SELECT_ATOMIC_SET;
import static com.anli.simpleorm.test.MockMySqlQueries.SELECT_ROOT_BY_PRIMARY_KEY;
import static com.anli.simpleorm.test.MockMySqlQueries.SELECT_ROOT_EXISTENT_KEYS_MAIN;
import static com.anli.simpleorm.test.MockMySqlQueries.UNLINK_ATOMIC_LIST_MAIN;
import static com.anli.simpleorm.test.MockMySqlQueries.UNLINK_ATOMIC_SET_MAIN;
import static com.anli.simpleorm.test.MockMySqlQueries.UPDATE_SUPER;
import static com.anli.simpleorm.test.TestDescriptorManagerBuilder.getTestManager;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class MySqlEngineTest {

    protected MockSqlExecutor sqlExecutor;
    protected MySqlEngine engine;
    protected CollectionFieldDescriptor atomicSetDescriptor;
    protected CollectionFieldDescriptor atomicListDescriptor;

    @Before
    public void setUp() {
        sqlExecutor = new MockSqlExecutor();
        UnitDescriptorManager manager = getTestManager();
        engine = new MySqlEngine(manager, sqlExecutor, new MySqlQueryBuilder());
        atomicSetDescriptor = (CollectionFieldDescriptor) manager
                .getDescriptor(ConcreteA.class).getField("atomicSet");
        atomicListDescriptor = (CollectionFieldDescriptor) manager
                .getDescriptor(ConcreteB.class).getField("atomicList");
    }

    @Test
    public void testGetByPrimaryKey_shouldReturnRow() {
        MockRow mockRow = new MockRow();
        mockRow.add("root_id", BigInteger.valueOf(1));
        mockRow.add("super_id", BigInteger.valueOf(1));
        mockRow.add("super_number", BigDecimal.valueOf(100));
        mockRow.add("concretea_id", BigInteger.valueOf(1));
        mockRow.add("concretea_time", new DateTime(2015, 5, 19, 0, 0));
        mockRow.add("concretea_atomic", BigInteger.valueOf(5));
        sqlExecutor.bindResult(SELECT_ROOT_BY_PRIMARY_KEY,
                asList(BigInteger.valueOf(1)), asList(mockRow));
        DataRow row = engine.getByPrimaryKey(BigInteger.valueOf(1), Root.class);
        assertEquals(BigInteger.valueOf(1), row.get("Root.id"));
        assertEquals(BigInteger.valueOf(1), row.get("Super.id"));
        assertEquals(BigDecimal.valueOf(100), row.get("Super.number"));
        assertEquals(BigInteger.valueOf(1), row.get("ConcreteA.id"));
        assertEquals(new DateTime(2015, 5, 19, 0, 0), row.get("ConcreteA.time"));
        assertEquals(BigInteger.valueOf(5), row.get("ConcreteA.atomic"));
        assertNull(row.get("ConcreteB.id"));
        assertNull(row.get("ConcreteB.name"));
        assertNull(row.get("ConcreteB.atomic"));
    }

    @Test
    public void testGetByPrimaryKey_shouldReturnNull() {
        sqlExecutor
                .bindResult(SELECT_ROOT_BY_PRIMARY_KEY, asList(BigInteger.valueOf(100)), (List) emptyList());
        DataRow row = engine.getByPrimaryKey(BigInteger.valueOf(100), Root.class);
        assertNull(row);
    }

    @Test
    public void testGetNonExistentKeys_shouldReturnKeys() {
        List<MockRow> mockRows = new LinkedList<>();
        MockRow mockRow = new MockRow();
        mockRow.add("root_id", BigInteger.valueOf(1));
        mockRows.add(mockRow);
        mockRow = new MockRow();
        mockRow.add("root_id", BigInteger.valueOf(2));
        mockRows.add(mockRow);
        List<BigInteger> keys = asList(BigInteger.valueOf(1), BigInteger.valueOf(2),
                BigInteger.valueOf(100), BigInteger.valueOf(200));
        sqlExecutor.bindResult(SELECT_ROOT_EXISTENT_KEYS_MAIN + "(?, ?, ?, ?)", keys, mockRows);
        Set<BigInteger> nonexistent = engine.getNonExistentKeys(keys, Root.class);
        assertTrue(nonexistent.size() == 2);
        assertTrue(nonexistent.contains(BigInteger.valueOf(100)));
        assertTrue(nonexistent.contains(BigInteger.valueOf(200)));
    }

    @Test
    public void testGetNonExistentKeys_shouldReturnEmptySet() {
        List<MockRow> mockRows = new LinkedList<>();
        MockRow mockRow = new MockRow();
        mockRow.add("root_id", BigInteger.valueOf(1));
        mockRows.add(mockRow);
        mockRow = new MockRow();
        mockRow.add("root_id", BigInteger.valueOf(2));
        mockRows.add(mockRow);
        mockRow = new MockRow();
        mockRow.add("root_id", BigInteger.valueOf(3));
        mockRows.add(mockRow);
        List<BigInteger> keys = asList(BigInteger.valueOf(1), BigInteger.valueOf(2),
                BigInteger.valueOf(3));
        sqlExecutor.bindResult(SELECT_ROOT_EXISTENT_KEYS_MAIN + "(?, ?, ?)", keys, mockRows);
        Set<BigInteger> nonexistent = engine.getNonExistentKeys(keys, Root.class);
        assertTrue(nonexistent.isEmpty());
    }

    @Test
    public void testGetCollectionKeys_shouldReturnKeyList() {
        List<MockRow> mockRows = new LinkedList<>();
        MockRow mockRow = new MockRow();
        mockRow.add("atomic_id", BigInteger.valueOf(5));
        mockRows.add(mockRow);
        mockRow = new MockRow();
        mockRow.add("atomic_id", BigInteger.valueOf(6));
        mockRows.add(mockRow);
        mockRow = new MockRow();
        mockRow.add("atomic_id", BigInteger.valueOf(7));
        mockRows.add(mockRow);
        sqlExecutor.bindResult(SELECT_ATOMIC_SET, asList(BigInteger.valueOf(1)), mockRows);
        Collection<BigInteger> keys = engine.getCollectionKeys(atomicSetDescriptor,
                BigInteger.valueOf(1));
        assertTrue(keys instanceof List);
        assertTrue(keys.size() == 3);
        assertTrue(keys.contains(BigInteger.valueOf(5)));
        assertTrue(keys.contains(BigInteger.valueOf(6)));
        assertTrue(keys.contains(BigInteger.valueOf(7)));
    }

    @Test
    public void testGetCollectionKeys_shouldReturnEmptyList() {
        sqlExecutor.bindResult(SELECT_ATOMIC_SET,
                asList(BigInteger.valueOf(2)), (List) emptyList());
        Collection<BigInteger> keys = engine.getCollectionKeys(atomicSetDescriptor,
                BigInteger.valueOf(2));
        assertTrue(keys instanceof List);
        assertTrue(keys.isEmpty());
    }

    @Test
    public void testGetCollectionData_shouldReturnMap() {
        List<MockRow> mockRows = new LinkedList<>();
        MockRow mockRow = new MockRow();
        mockRow.add("atomic_id", BigInteger.valueOf(7));
        mockRow.add("atomic_name", "SevenName");
        mockRows.add(mockRow);
        mockRow = new MockRow();
        mockRow.add("atomic_id", BigInteger.valueOf(6));
        mockRow.add("atomic_name", "SixName");
        mockRows.add(mockRow);
        sqlExecutor.bindResult(SELECT_ATOMIC_BY_KEYS_MAIN + "(?, ?, ?)",
                asList(BigInteger.valueOf(6), BigInteger.valueOf(7), BigInteger.valueOf(200)), mockRows);
        Map<Object, DataRow> data = engine.getCollectionData(Atomic.class, asList(BigInteger.valueOf(6),
                BigInteger.valueOf(7), BigInteger.valueOf(200)));
        assertTrue(data.size() == 2);
        DataRow row6 = data.get(BigInteger.valueOf(6));
        assertEquals(BigInteger.valueOf(6), row6.get("Atomic.id"));
        assertEquals("SixName", row6.get("Atomic.name"));
        DataRow row7 = data.get(BigInteger.valueOf(7));
        assertEquals(BigInteger.valueOf(7), row7.get("Atomic.id"));
        assertEquals("SevenName", row7.get("Atomic.name"));
    }

    @Test
    public void testGetCollectionData_shouldReturnEmptyMap() {
        sqlExecutor.bindResult(SELECT_ATOMIC_BY_KEYS_MAIN + "(?)",
                asList(BigInteger.valueOf(200)), (List) emptyList());
        Map<Object, DataRow> data = engine.getCollectionData(Atomic.class,
                asList(BigInteger.valueOf(200)));
        assertTrue(data.isEmpty());
    }

    @Test
    public void testExists_shouldReturnTrue() {
        MockRow row = new MockRow();
        row.add("Atomic.id", BigInteger.valueOf(6));
        sqlExecutor.bindResult(SELECT_ATOMIC_EXISTENT_KEYS_MAIN + "(?)",
                asList(BigInteger.valueOf(6)), asList(row));
        boolean result = engine.exists(BigInteger.valueOf(6), Atomic.class);
        assertTrue(result);
    }

    @Test
    public void testExists_shouldReturnFalse() {
        sqlExecutor.bindResult(SELECT_ATOMIC_EXISTENT_KEYS_MAIN + "(?)",
                asList(BigInteger.valueOf(200)), (List) emptyList());
        boolean result = engine.exists(BigInteger.valueOf(200), Atomic.class);
        assertFalse(result);
    }

    @Test
    public void testInsertAnemicEntity_shouldDoInserts() {
        engine.insertAnemicEntity(BigInteger.valueOf(1), Super.class);
        List<MockQueryKey> inserts = sqlExecutor.getExecutedUpdates();
        assertTrue(inserts.size() == 2);
        String rootInsert = inserts.get(0).getQuery();
        String superInsert = inserts.get(1).getQuery();
        List rootParams = inserts.get(0).getParameters();
        List superParams = inserts.get(1).getParameters();
        assertEquals(INSERT_ANEMIC_ROOT, rootInsert);
        assertEquals(INSERT_ANEMIC_SUPER, superInsert);
        assertTrue(rootParams.size() == 1);
        assertTrue(superParams.size() == 1);
        assertEquals(BigInteger.valueOf(1), rootParams.get(0));
        assertEquals(BigInteger.valueOf(1), superParams.get(0));
    }

    @Test
    public void testUpdateEntity_shouldDoUpdate() {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("Super.id", BigInteger.valueOf(1));
        paramMap.put("Super.number", BigDecimal.valueOf(100));
        engine.updateEntity(paramMap, Super.class);
        List<MockQueryKey> updates = sqlExecutor.getExecutedUpdates();
        assertTrue(updates.size() == 1);
        String update = updates.get(0).getQuery();
        List params = updates.get(0).getParameters();
        assertEquals(UPDATE_SUPER, update);
        assertTrue(params.size() == 2);
        Object number = params.get(0);
        Object id = params.get(1);
        assertEquals(BigDecimal.valueOf(100), number);
        assertEquals(BigInteger.valueOf(1), id);
    }

    @Test
    public void testDeleteEntity_shouldDoDelete() {
        engine.delete(BigInteger.valueOf(1), Root.class);
        List<MockQueryKey> deletes = sqlExecutor.getExecutedUpdates();
        assertTrue(deletes.size() == 1);
        String delete = deletes.get(0).getQuery();
        List params = deletes.get(0).getParameters();
        assertEquals(DELETE_ROOT, delete);
        assertTrue(params.size() == 1);
        Object id = params.get(0);
        assertEquals(BigInteger.valueOf(1), id);
    }

    @Test
    public void testUpdateLinkage_shouldDoLinkAndUnlinkOrdered() {
        engine.updateCollectionLinkage(atomicListDescriptor, asList(BigInteger.valueOf(6),
                BigInteger.valueOf(7), BigInteger.valueOf(8)), BigInteger.valueOf(1));
        List<MockQueryKey> updates = sqlExecutor.getExecutedUpdates();
        assertTrue(updates.size() == 2);
        String link = updates.get(0).getQuery();
        String unlink = updates.get(1).getQuery();
        List linkParams = updates.get(0).getParameters();
        List unlinkParams = updates.get(1).getParameters();
        String etalonLink = LINK_ATOMIC_LIST_MAIN
                + "select ? as key_column, 0 as order_column from dual union all "
                + "select ? as key_column, 1 as order_column from dual union all "
                + "select ? as key_column, 2 as order_column from dual";
        assertEquals(etalonLink, link);
        assertEquals(UNLINK_ATOMIC_LIST_MAIN + "(?, ?, ?)", unlink);
        assertTrue(linkParams.size() == 4);
        assertEquals(BigInteger.valueOf(6), linkParams.get(0));
        assertEquals(BigInteger.valueOf(7), linkParams.get(1));
        assertEquals(BigInteger.valueOf(8), linkParams.get(2));
        assertEquals(BigInteger.valueOf(1), linkParams.get(3));
        assertTrue(unlinkParams.size() == 4);
        assertEquals(BigInteger.valueOf(1), unlinkParams.get(0));
        assertEquals(BigInteger.valueOf(6), unlinkParams.get(1));
        assertEquals(BigInteger.valueOf(7), unlinkParams.get(2));
        assertEquals(BigInteger.valueOf(8), unlinkParams.get(3));
    }

    @Test
    public void testUpdateLinkage_shouldDoClearOrdered() {
        engine.updateCollectionLinkage(atomicListDescriptor, emptyList(), BigInteger.valueOf(1));
        List<MockQueryKey> updates = sqlExecutor.getExecutedUpdates();
        assertTrue(updates.size() == 1);
        String clear = updates.get(0).getQuery();
        List clearParams = updates.get(0).getParameters();
        assertEquals(CLEAR_ATOMIC_LIST, clear);
        assertTrue(clearParams.size() == 1);
        assertEquals(BigInteger.valueOf(1), clearParams.get(0));
    }

    @Test
    public void testUpdateLinkage_shouldDoLinkAndUnlink() {
        engine.updateCollectionLinkage(atomicSetDescriptor, asList(BigInteger.valueOf(6),
                BigInteger.valueOf(7)), BigInteger.valueOf(1));
        List<MockQueryKey> updates = sqlExecutor.getExecutedUpdates();
        assertTrue(updates.size() == 2);
        String link = updates.get(0).getQuery();
        String unlink = updates.get(1).getQuery();
        List linkParams = updates.get(0).getParameters();
        List unlinkParams = updates.get(1).getParameters();
        assertEquals(LINK_ATOMIC_SET_MAIN + "(?, ?)", link);
        assertEquals(UNLINK_ATOMIC_SET_MAIN + "(?, ?)", unlink);
        assertTrue(linkParams.size() == 3);
        assertEquals(BigInteger.valueOf(1), linkParams.get(0));
        assertEquals(BigInteger.valueOf(6), linkParams.get(1));
        assertEquals(BigInteger.valueOf(7), linkParams.get(2));
        assertTrue(unlinkParams.size() == 3);
        assertEquals(BigInteger.valueOf(1), unlinkParams.get(0));
        assertEquals(BigInteger.valueOf(6), unlinkParams.get(1));
        assertEquals(BigInteger.valueOf(7), unlinkParams.get(2));
    }

    @Test
    public void testUpdateLinkage_shouldDoClear() {
        engine.updateCollectionLinkage(atomicSetDescriptor, emptyList(), BigInteger.valueOf(1));
        List<MockQueryKey> updates = sqlExecutor.getExecutedUpdates();
        assertTrue(updates.size() == 1);
        String clear = updates.get(0).getQuery();
        List clearParams = updates.get(0).getParameters();
        assertEquals(CLEAR_ATOMIC_SET, clear);
        assertTrue(clearParams.size() == 1);
        assertEquals(BigInteger.valueOf(1), clearParams.get(0));
    }
}
