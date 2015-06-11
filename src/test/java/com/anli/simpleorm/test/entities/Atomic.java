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

    public void setId(BigInteger id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }
}
