package com.anli.simpleorm.controller;

public interface PrimaryKeyGenerator {

    Object getPrimaryKey(Class entityClass);
}
