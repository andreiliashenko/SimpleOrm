package com.anli.simpleorm.test.entities;

import java.math.BigInteger;

public class Atomic implements IAtomic {

    protected BigInteger id;
    protected String name;

    @Override
    public BigInteger getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }
}
