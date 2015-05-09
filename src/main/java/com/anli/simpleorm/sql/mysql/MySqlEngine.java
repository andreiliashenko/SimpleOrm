package com.anli.simpleorm.sql.mysql;

import com.anli.simpleorm.descriptors.UnitDescriptorManager;
import com.anli.simpleorm.queries.QueryDescriptor;
import com.anli.simpleorm.queries.mysql.MySqlQueryBuilder;
import com.anli.simpleorm.sql.SqlEngine;
import com.anli.sqlexecution.execution.SqlExecutor;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MySqlEngine extends SqlEngine {

    protected final MySqlQueryBuilder queryBuilder;

    public MySqlEngine(UnitDescriptorManager descriptorManager, SqlExecutor executor,
            MySqlQueryBuilder queryBuilder) {
        super(descriptorManager, executor);
        this.queryBuilder = queryBuilder;
    }

    @Override
    protected List resolveParameters(QueryDescriptor query, Map<String, Object> parameters) {
        sdggdd
    }

    @Override
    protected QueryDescriptor resolveQuery(QueryDescriptor sourceDescriptor, 
            Map<String, Object> parameters) {
        return getQueryBuilder().resolveMacros(sourceDescriptor, parameters);
    }
    
    protected MySqlQueryBuilder getQueryBuilder() {
        return queryBuilder;
    }
}
