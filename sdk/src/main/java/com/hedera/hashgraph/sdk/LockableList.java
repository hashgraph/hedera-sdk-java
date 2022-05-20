package com.hedera.hashgraph.sdk;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Internal utility class for a new lockable list type.
 *
 * @param <T>
 */
class LockableList<T> implements Iterable<T> {
    private ArrayList<T> list = new ArrayList<>();
    private int index = 0;
    private boolean locked = false;

    /**
     * Constructor.
     */
    LockableList() {
    }

    /**
     * Assign a list of items to this list instance.
     *
     * @param list                      the lockable list
     */
    LockableList(ArrayList<T> list) {
        this.list = list;
    }

    /**
     * Verify that this list instance is not locked.
     */
    void requireNotLocked() {
        if (locked) {
            throw new IllegalStateException("Cannot modify a locked list");
        }
    }

    /**
     * Make sure that this list instance has the requested capacity.
     *
     * @param capacity                  the minimum capacity
     * @return                          the updated list
     */
    LockableList<T> ensureCapacity(int capacity) {
        list.ensureCapacity(capacity);
        return this;
    }

    /**
     * Assign a list to this list instance.
     *
     * @param list                      the lockable list to assign
     * @return                          the updated list
     */
    LockableList<T> setList(List<T> list) {
        requireNotLocked();
        this.list = new ArrayList<>(list);
        this.index = 0;
        return this;
    }

    /**
     * @return                          the lockable list
     */
    ArrayList<T> getList() {
        return list;
    }

    /**
     * Add items to this list instance.
     *
     * @param elements                  the items to add
     * @return                          the updated list
     */
    LockableList<T> add(T ...elements) {
        requireNotLocked();

        for (var e : elements) {
            list.add(e);
        }

        return this;
    }

    /**
     * Remove an item from this list instance.
     *
     * @param element                   the element to remove
     * @return                          the updated list
     */
    LockableList<T> remove(T element) {
        requireNotLocked();
        list.remove(element);
        return this;
    }

    /**
     * @return                          the current list item
     */
    T getCurrent() {
        return get(index);
    }

    /**
     * @return                          the next list item
     */
    T getNext() {
        return get(advance());
    }

    /**
     * Get a specific list item.
     *
     * @param index                     the index of the item
     * @return                          the item
     */
    T get(int index) {
        return list.get(index);
    }

    /**
     * Assign an item at the specified index.
     *
     * @param index                     the index of the item
     * @param item                      the item
     * @return                          the updated list
     */
    LockableList<T> set(int index, T item) {
        requireNotLocked();

        if (index == list.size()) {
            list.add(item);
        } else {
            list.set(index, item);
        }

        return this;
    }

    /**
     * Assign an item to the list if:
     * - item at index is null
     * - index is not greater than size of existing list
     *
     * @param index                     the requested index
     * @param item                      the item
     * @return                          the updated list
     */
    LockableList<T> setIfAbsent(int index, T item) {
        if (index == list.size() || list.get(index) == null) {
            set(index, item);
        }

        return this;
    }

    /**
     * @return                          the next index wrapped
     */
    int advance() {
        var index = this.index;
        this.index = (this.index + 1) % list.size();
        return index;
    }

    /**
     * @return                          is the list empty
     */
    boolean isEmpty() {
        return list.isEmpty();
    }

    /**
     * @return                          is the list locked
     */
    boolean isLocked() {
        return locked;
    }

    /**
     * Assign the lock status.
     *
     * @param locked                    the lock status
     * @return                          the updated list
     */
    LockableList<T> setLocked(boolean locked) {
        this.locked = locked;
        return this;
    }

    /**
     * @return                          the size of the list
     */
    int size() {
        return list.size();
    }

    /**
     * Assign the current list index.
     *
     * @param index                     the index
     * @return                          the updated list
     */
    LockableList<T> setIndex(int index) {
        this.index = index;
        return this;
    }

    /**
     * @return                          the index of the current item
     */
    int getIndex() {
        return index;
    }

    /**
     * @return                          an empty list
     */
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
