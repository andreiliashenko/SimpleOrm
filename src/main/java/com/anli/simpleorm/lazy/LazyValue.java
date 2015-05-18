package com.anli.simpleorm.lazy;

import java.util.Objects;

public class LazyValue<K, V> {

    protected K key;
    protected V value;
    protected boolean loaded;
    protected final Loader<K, V> loader;

    public LazyValue(V value, Loader<K, V> loader) {
        this.key = value != null ? loader.extractKey(value) : null;
        this.value = value;
        this.loaded = true;
        this.loader = loader;
    }

    public LazyValue(Loader<K, V> loader, K key) {
        this.key = key;
        this.value = null;
        this.loaded = false;
        this.loader = loader;
    }

    public V setValue(V value) {
        V oldValue = getValue();
        this.key = value != null ? loader.extractKey(value) : null;
        this.value = value;
        this.loaded = true;
        return oldValue;
    }

    public V getValue() {
        if (key == null) {
            return null;
        }
        if (!loaded) {
            value = loader.get(key);
            loaded = true;
        }
        return value;
    }

    public K getKey() {
        return key;
    }

    public boolean isLoaded() {
        return loaded;
    }

    @Override
    public boolean equals(Object obj) {
        V effectiveValue = getValue();
        if (effectiveValue == null) {
            return false;
        }
        return effectiveValue.equals(obj);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.value);
        return hash;
    }
}
