package com.hedera.hashgraph.sdk;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class LockableList<T> implements Iterable<T> {
    private ArrayList<T> list = new ArrayList<>();
    private int index = 0;
    private boolean locked = false;

    LockableList() {
    }

    LockableList(ArrayList<T> list) {
        this.list = list;
    }

    void requireNotLocked() {
        if (locked) {
            throw new IllegalStateException("Cannot modify a locked list");
        }
    }

    LockableList<T> ensureCapacity(int capacity) {
        list.ensureCapacity(capacity);
        return this;
    }

    LockableList<T> setList(List<T> list) {
        requireNotLocked();
        this.list = new ArrayList<>(list);
        this.index = 0;
        return this;
    }

    ArrayList<T> getList() {
        return list;
    }

    LockableList<T> add(T ...elements) {
        requireNotLocked();

        for (var e : elements) {
            list.add(e);
        }

        return this;
    }

    LockableList<T> remove(T element) {
        requireNotLocked();
        list.remove(element);
        return this;
    }

    T getCurrent() {
        return get(index);
    }

    T getNext() {
        return get(advance());
    }

    T get(int index) {
        return list.get(index);
    }

    LockableList<T> set(int index, T item) {
        requireNotLocked();

        if (index == list.size()) {
            list.add(item);
        } else {
            list.set(index, item);
        }

        return this;
    }

    LockableList<T> setIfAbsent(int index, T item) {
        if (index == list.size() || list.get(index) == null) {
            set(index, item);
        }

        return this;
    }

     int advance() {
        var index = this.index;
        this.index = (this.index + 1) % list.size();
        return index;
    }

    boolean isEmpty() {
        return list.isEmpty();
    }

    boolean isLocked() {
        return locked;
    }

    LockableList<T> setLocked(boolean locked) {
        this.locked = locked;
        return this;
    }

    int size() {
        return list.size();
    }

    LockableList<T> setIndex(int index) {
        this.index = index;
        return this;
    }

    int getIndex() {
        return index;
    }

    LockableList<T> clear() {
        requireNotLocked();
        list.clear();
        return this;
    }

    @Override
    public Iterator<T> iterator() {
        return list.iterator();
    }
}
