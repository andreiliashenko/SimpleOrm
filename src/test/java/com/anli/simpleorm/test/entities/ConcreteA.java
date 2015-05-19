package com.anli.simpleorm.test.entities;

import java.util.Set;
import org.joda.time.DateTime;

public class ConcreteA extends Super {

    protected DateTime time;
    protected Atomic atomic;
    protected Set<Atomic> atomicSet;

    public DateTime getTime() {
        return time;
    }

    public void setTime(DateTime time) {
        this.time = time;
    }

    public IAtomic getAtomic() {
        return atomic;
    }

    public void setAtomic(IAtomic atomic) {
        this.atomic = (Atomic) atomic;
    }

    public IAtomic setAtomic() {
        IAtomic oldAtomic = this.atomic;
        this.atomic = null;
        return oldAtomic;
    }

    public Set<IAtomic> getAtomicSet() {
        return (Set) atomicSet;
    }

    public void setAtomicSet(Set<IAtomic> atomicSet) {
        this.atomicSet = (Set) atomicSet;
    }
}
