package com.anli.simpleorm.test;

import com.anli.simpleorm.controller.PrimaryKeyGenerator;
import java.math.BigInteger;

public class TestKeyGenerator implements PrimaryKeyGenerator {

    public static final TestKeyGenerator GENERATOR = new TestKeyGenerator();

    protected volatile BigInteger lastKey;

    private TestKeyGenerator() {
        lastKey = BigInteger.ZERO;
    }

    public void setLastKey(BigInteger lastKey) {
        this.lastKey = lastKey;
    }

    @Override
    public Object getPrimaryKey(Class entityClass) {
        lastKey = lastKey.add(BigInteger.ONE);
        return lastKey;
    }
}
