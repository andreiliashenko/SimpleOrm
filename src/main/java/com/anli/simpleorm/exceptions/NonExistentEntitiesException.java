package com.anli.simpleorm.exceptions;

import java.util.List;

public class NonExistentEntitiesException extends RuntimeException {

    protected final List entities;

    public NonExistentEntitiesException(List entities) {
        super();
        this.entities = entities;
    }

    public List getEntities() {
        return entities;
    }
}
