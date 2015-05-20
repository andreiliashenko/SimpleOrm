package com.anli.simpleorm.sql;

import java.util.Map;
import java.util.TreeMap;

public class DataRow {

    private final Map<String, Object> map;

    public DataRow() {
        map = new TreeMap<>();
    }

    public Object get(String key) {
        return map.get(key);
    }

    public void put(String key, Object value) {
        map.put(key, value);
    }

    public int size() {
        return map.size();
    }
}
