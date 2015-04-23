package com.anli.simpleorm.exceptions;

import java.util.Collections;
import java.util.List;

public class NonExistentEntitiesException extends RuntimeException {

    protected final List entities;

    public NonExistentEntitiesException(List entities) {
        super();
        this.entities = entities;
    }

    public NonExistentEntitiesException(Object entity) {
        super();
        this.entities = Collections.singletonList(entity);
    }

    public List getEntities() {
        return entities;
    }
}
