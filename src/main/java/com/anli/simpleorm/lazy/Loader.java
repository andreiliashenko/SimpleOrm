package com.anli.simpleorm.lazy;

public interface Loader<K, V> {

    V get(K key);

    K extractKey(V value);
}
