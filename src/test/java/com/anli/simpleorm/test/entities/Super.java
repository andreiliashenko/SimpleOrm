package com.anli.simpleorm.test.entities;

import java.math.BigDecimal;

public class Super extends Root {

    protected BigDecimal number;

    public BigDecimal getNumber() {
        return number;
    }

    public void setNumber(BigDecimal number) {
        this.number = number;
    }
}
