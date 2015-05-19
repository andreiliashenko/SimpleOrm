package com.anli.simpleorm.test;

import com.anli.sqlexecution.execution.SqlExecutor;
import com.anli.sqlexecution.handling.ResultSetHandler;
import com.anli.sqlexecution.handling.TransformingResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MockSqlExecutor extends SqlExecutor {

    protected final Map<MockQueryKey, List<MockRow>> resultBinding;
    protected final List<MockQueryKey> executedUpdates;

    public MockSqlExecutor() {
        super(null, null);
        this.resultBinding = new HashMap<>();
        this.executedUpdates = new LinkedList<>();
    }

    @Override
    public <T> T executeSelect(String query, List parameters, ResultSetHandler<T> resultSetHandler) {
        MockQueryKey queryKey = new MockQueryKey(query, parameters);
        List<MockRow> rows = resultBinding.get(queryKey);
        try {
            return resultSetHandler.handle(new MockTransformingResultSet(rows));
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public int executeUpdate(String query, List parameters) {
        executedUpdates.add(new MockQueryKey(query, parameters));
        return 0;
    }

    public List<MockQueryKey> getExecutedUpdates() {
        return executedUpdates;
    }

    public void bindResult(String query, List parameters, List<MockRow> result) {
        resultBinding.put(new MockQueryKey(query, parameters), result);
    }

    public static class MockQueryKey {

        protected final String query;
        protected final List parameters;

        public MockQueryKey(String query, List parameters) {
            this.query = query;
            this.parameters = parameters;
        }

        @Override
        public boolean equals(Object other) {
            if (other == null) {
                return false;
            }
            if (!(other instanceof MockQueryKey)) {
                return false;
            }
            MockQueryKey otherKey = (MockQueryKey) other;
            boolean keyEquals = this.query != null
                    ? this.query.equals(otherKey.query) : otherKey.query == null;
            boolean paramEquals = this.parameters != null
                    ? this.parameters.equals(otherKey.parameters) : otherKey.parameters == null;
            return keyEquals && paramEquals;
        }

        public String getQuery() {
            return query;
        }

        public List getParameters() {
            return parameters;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 13 * hash + Objects.hashCode(this.query);
            hash = 13 * hash + Objects.hashCode(this.parameters);
            return hash;
        }
    }

    protected static class MockTransformingResultSet extends TransformingResultSet {

        protected final Iterator<MockRow> iterator;
        protected MockRow lastRow;

        public MockTransformingResultSet(List<MockRow> rows) {
            super(null, null);
            this.iterator = rows.iterator();
            this.lastRow = null;
        }

        @Override
        public <T> T getValue(String columnLabel, Class<T> javaClass) throws SQLException {
            return javaClass.cast(lastRow.getValue(columnLabel));
        }

        @Override
        public <T> T getValue(int columnIndex, Class<T> javaClass) throws SQLException {
            return javaClass.cast(lastRow.getValue(columnIndex));
        }

        @Override
        public boolean next() throws SQLException {
            boolean result = iterator.hasNext();
            if (result) {
                lastRow = iterator.next();
            }
            return result;
        }
    }

    public static class MockRow {

        protected final Map<String, Object> row = new HashMap<>();

        public void add(String key, Object value) {
            row.put(key, value);
        }

        public Object getValue(String key) {
            return row.get(key);
        }

        public Object getValue(int index) {
            return row.values().toArray()[index - 1];
        }
    }
}
