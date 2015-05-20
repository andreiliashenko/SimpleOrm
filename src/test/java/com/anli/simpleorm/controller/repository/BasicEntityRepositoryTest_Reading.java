package com.anli.simpleorm.controller.repository;

import com.anli.simpleorm.sql.DataRow;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.joda.time.DateTime;
import org.junit.Test;

public class BasicEntityRepositoryTest_Reading extends BasicEntityRepositoryTest {

    protected DataRow createAtomicRow(BigInteger id, String name) {
        DataRow row = new DataRow();
        row.put("Atomic.id", id);
        row.put("Atomic.name", name);
        return row;
    }

    protected DataRow createRootRow(BigInteger id) {
        DataRow row = new DataRow();
        row.put("Root.id", id);
        return row;
    }

    protected DataRow createSuperRow(BigInteger id, BigDecimal number) {
        DataRow row = createRootRow(id);
        row.put("Super.id", id);
        row.put("Super.number", number);
        return row;
    }

    protected DataRow createConcreteARow(BigInteger id, BigDecimal number,
            DateTime time, BigInteger atomic) {
        DataRow row = createSuperRow(id, number);
        row.put("ConcreteA.id", id);
        row.put("ConcreteA.time", time);
        row.put("ConcreteA.atomic", atomic);
        return row;
    }

    protected DataRow createConcreteBRow(BigInteger id, BigDecimal number,
            String name, BigInteger atomic) {
        DataRow row = createSuperRow(id, number);
        row.put("ConcreteB.id", id);
        row.put("ConcreteB.name", name);
        row.put("ConcreteB.atomic", atomic);
        return row;
    }

    protected void createAtomics() {
        sqlEngine.getAtomicMap().put(BigInteger.valueOf(1),
                createAtomicRow(BigInteger.valueOf(1), null));
        sqlEngine.getAtomicMap().put(BigInteger.valueOf(2),
                createAtomicRow(BigInteger.valueOf(2), "AT1"));
        sqlEngine.getAtomicMap().put(BigInteger.valueOf(3),
                createAtomicRow(BigInteger.valueOf(3), "AT2"));
        sqlEngine.getAtomicMap().put(BigInteger.valueOf(4),
                createAtomicRow(BigInteger.valueOf(4), "AT3"));
    }

    protected void createRoots() {
        sqlEngine.getRootMap().put(BigInteger.valueOf(5), createRootRow(BigInteger.valueOf(5)));
        sqlEngine.getRootMap().put(BigInteger.valueOf(6), createRootRow(BigInteger.valueOf(6)));
    }

    protected void createSupers() {
        DataRow superA = createSuperRow(BigInteger.valueOf(7), null);
        DataRow superB = createSuperRow(BigInteger.valueOf(8), BigDecimal.valueOf(100));
        sqlEngine.getRootMap().put(BigInteger.valueOf(7), superA);
        sqlEngine.getRootMap().put(BigInteger.valueOf(8), superB);
        sqlEngine.getSuperMap().put(BigInteger.valueOf(7), superA);
        sqlEngine.getSuperMap().put(BigInteger.valueOf(8), superB);
    }
    
    @Override
    public void setUp() {
        super.setUp();

    }
}
