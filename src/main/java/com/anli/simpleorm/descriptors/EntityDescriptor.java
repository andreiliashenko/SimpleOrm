package com.anli.simpleorm.descriptors;

import com.anli.simpleorm.controller.PrimaryKeyGenerator;
import com.anli.simpleorm.definitions.EntityDefinition;
import com.anli.simpleorm.reflective.EntityProcessor;

public class EntityDescriptor {

    public PrimaryKeyGenerator getPrimaryKeyGenerator() {
        return null;
    }
    
    public EntityProcessor getProcessor() {
        return null;
    }
    
    public EntityDefinition getDefinition() {
        return null;
    }
    
    public Class getPrimaryKeyClass() {
        return null;
    }
}
