package com.anli.simpleorm.lazy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public abstract class LazyCollection<K, V> implements Collection<V> {

    protected Loader<K, V> loader;

    protected final Collection<LazyValue<K, V>> holders;

    protected boolean dirty;

    protected LazyCollection(Loader<K, V> loader, Collection<K> keys) {
        this.loader = loader;
        holders = getHolderCollection(keys);
        dirty = false;
    }

    private Collection<LazyValue<K, V>> getHolderCollection(Collection<K> keys) {
        Collection<LazyValue<K, V>> emptyCollection = getEmptyHolderCollection(keys);
        for (K key : keys) {
            emptyCollection.add(new LazyValue<>(loader, key));
        }
        return emptyCollection;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void makeDirty() {
        dirty = true;
    }

    protected abstract Collection<LazyValue<K, V>> getEmptyHolderCollection(Collection<K> keys);

    @Override
    public int size() {
        return holders.size();
    }

    @Override
    public boolean isEmpty() {
        return holders.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return holders.contains(o);
    }

    @Override
    public Iterator<V> iterator() {
        return getNewIterator();
    }

    @Override
    public Object[] toArray() {
        Object[] array = new Object[holders.size()];
        int count = 0;
        for (LazyValue<K, V> holder : holders) {
            array[count] = holder.getValue();
            count++;
        }
        return array;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        int size = holders.size();
        T[] array = (a.length < size) ? (T[]) new Object[size] : a;
        int count = 0;
        for (LazyValue<K, V> holder : holders) {
            array[count] = (T) holder.getValue();
            count++;
        }
        while (count < array.length) {
            array[count] = null;
            count++;
        }
        return array;
    }

    @Override
    public boolean add(V e) {
        makeDirty();
        return holders.add(new LazyValue<>(e, loader));
    }

    @Override
    public boolean remove(Object o) {
        makeDirty();
        return holders.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return holders.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends V> c) {
        makeDirty();
        ArrayList<LazyValue<K, V>> values = new ArrayList<>(c.size());
        for (V element : c) {
            values.add(new LazyValue<>(element, loader));
        }
        return holders.addAll(values);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        makeDirty();
        return holders.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        makeDirty();
        return holders.retainAll(c);
    }

    @Override
    public void clear() {
        makeDirty();
        holders.clear();
    }

    protected class LazyIterator implements Iterator<V> {

        protected Iterator<LazyValue<K, V>> lazyIterator;

        public LazyIterator(Iterator<LazyValue<K, V>> lazyIterator) {
            this.lazyIterator = lazyIterator;
        }

        @Override
        public boolean hasNext() {
            return lazyIterator.hasNext();
        }

        @Override
        public V next() {
            return lazyIterator.next().getValue();
        }

        @Override
        public void remove() {
            lazyIterator.remove();
        }
    }

    protected LazyIterator getNewIterator() {
        return new LazyIterator(holders.iterator());
    }

    public ArrayList<K> getKeys() {
        ArrayList<K> list = new ArrayList<>(holders.size());
        for (LazyValue<K, V> holder : holders) {
            list.add(holder.getKey());
        }
        return list;
    }
}
