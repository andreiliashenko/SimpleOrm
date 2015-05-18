package com.anli.simpleorm.lazy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class LazyList<K, V> extends LazyCollection<K, V> implements List<V> {

    public LazyList(Loader<K, V> loader, List<K> keys) {
        super(loader, keys);
    }

    protected List<LazyValue<K, V>> getHoldersList() {
        return (List) holders;
    }

    @Override
    protected Collection<LazyValue<K, V>> getEmptyHolderCollection(Collection<K> keys) {
        if (keys instanceof ArrayList) {
            return new ArrayList<>(keys.size());
        }
        if (keys instanceof LinkedList) {
            return new LinkedList<>();
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int index, Collection<? extends V> c) {
        makeDirty();
        ArrayList<LazyValue<K, V>> values = new ArrayList<>(c.size());
        for (V element : c) {
            values.add(new LazyValue<>(element, loader));
        }
        return getHoldersList().addAll(index, values);
    }

    @Override
    public V get(int index) {
        return getHoldersList().get(index).getValue();
    }

    @Override
    public V set(int index, V element) {
        makeDirty();
        return getHoldersList().get(index).setValue(element);
    }

    @Override
    public void add(int index, V element) {
        makeDirty();
        getHoldersList().add(index, new LazyValue<>(element, loader));
    }

    @Override
    public V remove(int index) {
        makeDirty();
        return getHoldersList().remove(index).getValue();
    }

    @Override
    public int indexOf(Object o) {
        return getHoldersList().indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return getHoldersList().lastIndexOf(o);
    }

    @Override
    public ListIterator<V> listIterator() {
        return new LazyListIterator(getHoldersList().listIterator());
    }

    @Override
    public ListIterator<V> listIterator(int index) {
        return new LazyListIterator(getHoldersList().listIterator(index));
    }

    @Override
    public List<V> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException();
    }

    protected class LazyListIterator extends LazyIterator implements ListIterator<V> {

        public LazyListIterator(ListIterator<LazyValue<K, V>> lazyIterator) {
            super(lazyIterator);
        }

        @Override
        public boolean hasPrevious() {
            return getListIterator().hasPrevious();
        }

        @Override
        public V previous() {
            return getListIterator().previous().getValue();
        }

        @Override
        public int nextIndex() {
            return getListIterator().nextIndex();
        }

        @Override
        public int previousIndex() {
            return getListIterator().previousIndex();
        }

        @Override
        public void remove() {
            LazyList.this.makeDirty();
            getListIterator().remove();
        }

        @Override
        public void set(V e) {
            LazyList.this.makeDirty();
            getListIterator().set(new LazyValue<>(e, loader));
        }

        @Override
        public void add(V e) {
            LazyList.this.makeDirty();
            getListIterator().add(new LazyValue<>(e, loader));
        }

        protected ListIterator<LazyValue<K, V>> getListIterator() {
            return (ListIterator) lazyIterator;
        }
    }
}
