package com.anli.simpleorm.lazy;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class LazySet<K, V> extends LazyCollection<K, V> implements Set<V> {

    public LazySet(Loader<K, V> loader, Set<K> keys) {
        super(loader, keys);
    }

    @Override
    protected Collection<LazyValue<K, V>> getEmptyHolderCollection(Collection<K> keys) {
        if (keys instanceof HashSet) {
            return new HashSet<>((int) (keys.size() / 0.75f), 0.75f);
        }
        throw new UnsupportedOperationException();
    }
}
