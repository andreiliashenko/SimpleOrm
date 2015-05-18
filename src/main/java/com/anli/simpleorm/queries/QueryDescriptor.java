package com.anli.simpleorm.queries;

import java.util.Map;

public class QueryDescriptor {

    protected final String query;
    protected final Map<String, Integer> parameterBindings;
    protected final Map<String, String> resultBindings;

    public QueryDescriptor(String query, Map<String, Integer> parameterBindings,
            Map<String, String> resultBindings) {
        this.query = query;
        this.parameterBindings = parameterBindings;
        this.resultBindings = resultBindings;
    }

    public String getQuery() {
        return query;
    }

    public String getResultBinding(String field) {
        return resultBindings.get(field);
    }

    public Map<String, String> getResultBindings() {
        return resultBindings;
    }

    public int getParameterBinding(String field) {
        return parameterBindings.get(field);
    }

    public Map<String, Integer> getParameterBindings() {
        return parameterBindings;
    }
}
