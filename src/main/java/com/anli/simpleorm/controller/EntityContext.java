package com.anli.simpleorm.controller;

public interface EntityContext extends EntityController {

    void flush();

    void close();
}
