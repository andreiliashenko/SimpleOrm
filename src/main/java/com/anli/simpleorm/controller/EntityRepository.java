package com.anli.simpleorm.controller;

public interface EntityRepository extends EntityController {

    <E> E save(E entity);

    <E> E pull(E entity, String lazyFieldName);
}
