package com.anli.simpleorm.sql.mysql;

import com.anli.simpleorm.descriptors.UnitDescriptorManager;
import com.anli.simpleorm.queries.QueryDescriptor;
import com.anli.simpleorm.queries.mysql.MySqlQueryBuilder;
import com.anli.simpleorm.sql.SqlEngine;
import com.anli.sqlexecution.execution.SqlExecutor;
import java.util.ArrayList;
import java.util.Collection;
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
        TreeMap<Integer, Object> paramMap = new TreeMap<>();
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            Object value = entry.getValue();
            String field = entry.getKey();
            if (value instanceof Collection) {
                Collection collectionValue = (Collection) value;
                int index = query.getParameterBinding(field);
                for (Object element : collectionValue) {
                    paramMap.put(index, element);
                    index++;
                }
            } else {
                paramMap.put(query.getParameterBinding(field), value);
            }
        }
        return new ArrayList(paramMap.values());
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
