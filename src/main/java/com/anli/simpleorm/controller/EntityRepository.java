package com.anli.simpleorm.controller;

public interface EntityRepository extends EntityController {

    <E> void save(E entity);

    <E> void pull(E entity, String lazyFieldName);
}
