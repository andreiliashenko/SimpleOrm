package com.anli.simpleorm.test.entities;

import java.util.List;

public class ConcreteB extends Super {

    protected String name;
    protected Atomic atomic;
    protected List<Atomic> atomicList;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Atomic getAtomic() {
        return atomic;
    }

    public void setAtomic(Atomic atomic) {
        this.atomic = atomic;
    }

    public List<Atomic> getAtomicList() {
        return atomicList;
    }
}
