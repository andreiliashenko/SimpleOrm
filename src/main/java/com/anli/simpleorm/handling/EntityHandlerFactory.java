package com.anli.simpleorm.handling;

public interface EntityHandlerFactory {

    public <Entity> EntityHandler<Entity> getHandler(String entityName);
}
