package com.anli.simpleorm.cache;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class HierarchicalCache<Key, Value> {

    protected final Map<Class, CacheNode> cacheNodes;

    public HierarchicalCache() {
        cacheNodes = new HashMap<>();
    }

    public Value get(Class clazz, Key key) {
        CacheNode node = cacheNodes.get(clazz);
        if (node == null) {
            return null;
        }
        return node.get(key);
    }

    public void put(Class clazz, Key key, Value value) {
        CacheNode node = cacheNodes.get(clazz);
        if (node == null) {
            node = createNode(clazz);
            cacheNodes.put(clazz, node);
        }
        node.put(key, value);
    }

    protected CacheNode createNode(Class nodeClass) {
        CacheNode node = new CacheNode();
        Iterable<Map.Entry<Class, CacheNode>> parents =
                Iterables.filter(cacheNodes.entrySet(), new ParentPredicate(nodeClass));
        for (Map.Entry<Class, CacheNode> entry : parents) {
            node.addParent(entry.getValue());
        }
        Iterable<Map.Entry<Class, CacheNode>> children =
                Iterables.filter(cacheNodes.entrySet(), new ChildPredicate(nodeClass));
        for (Map.Entry<Class, CacheNode> entry : children) {
            entry.getValue().addParent(node);
        }
        return node;
    }

    protected class CacheNode {

        protected final Map<Key, Value> map;
        protected final List<CacheNode> parents;

        public CacheNode() {
            this.map = new HashMap<>();
            this.parents = new LinkedList<>();
        }

        public void addParent(CacheNode node) {
            parents.add(node);
            map.putAll(node.map);
        }

        public Value get(Key key) {
            return map.get(key);
        }

        public void put(Key key, Value value) {
            map.put(key, value);
            for (CacheNode node : parents) {
                node.map.put(key, value);
            }
        }
    }

    protected class ParentPredicate implements Predicate<Map.Entry<Class, CacheNode>> {

        protected final Class child;

        public ParentPredicate(Class child) {
            this.child = child;
        }

        @Override
        public boolean apply(Map.Entry<Class, CacheNode> input) {
            return input.getKey().isAssignableFrom(child);
        }
    }

    protected class ChildPredicate implements Predicate<Map.Entry<Class, CacheNode>> {

        protected final Class base;

        public ChildPredicate(Class base) {
            this.base = base;
        }

        @Override
        public boolean apply(Map.Entry<Class, CacheNode> input) {
            return base.isAssignableFrom(input.getKey());
        }
    }
}
