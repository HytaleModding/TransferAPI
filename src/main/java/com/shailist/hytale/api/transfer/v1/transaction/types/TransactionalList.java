package com.shailist.hytale.api.transfer.v1.transaction.types;

import com.shailist.hytale.api.transfer.v1.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Helper class that implements a transactional {@link List}.
 * Extends {@link TransactionalValue} and provides transaction aware implementations for all modifying methods of {@link List}.
 * @param <T> The type of the list's elements.
 */
public class TransactionalList<T> extends TransactionalValue<List<T>> {
    public TransactionalList(@NotNull List<T> startingValue) {
        super(startingValue);
    }

    @Override
    protected @NotNull List<T> createSnapshot() {
        // Since List<T> is mutable, we must return a clone, or at least a new list
        return new ArrayList<>(value);
    }

    public boolean add(T t, @NotNull TransactionContext transaction) {
        updateSnapshots(transaction);
        return value.add(t);
    }

    public boolean remove(T o, @NotNull TransactionContext transaction) {
        updateSnapshots(transaction);
        return value.remove(o);
    }


    public boolean addAll(@NotNull Collection<? extends T> c, @NotNull TransactionContext transaction) {
        updateSnapshots(transaction);
        return value.addAll(c);
    }

    public boolean addAll(int index, @NotNull Collection<? extends T> c, @NotNull TransactionContext transaction) {
        updateSnapshots(transaction);
        return value.addAll(index, c);
    }

    public boolean removeAll(@NotNull Collection<? extends T> c, @NotNull TransactionContext transaction) {
        updateSnapshots(transaction);
        return value.removeAll(c);
    }

    public boolean retainAll(@NotNull Collection<? extends T> c, @NotNull TransactionContext transaction) {
        updateSnapshots(transaction);
        return value.retainAll(c);
    }

    public void clear(@NotNull TransactionContext transaction) {
        updateSnapshots(transaction);
        value.clear();
    }

    public T set(int index, T element, @NotNull TransactionContext transaction) {
        updateSnapshots(transaction);
        return value.set(index, element);
    }

    public void add(int index, T element, @NotNull TransactionContext transaction) {
        updateSnapshots(transaction);
        value.add(index, element);
    }

    public T remove(int index, @NotNull TransactionContext transaction) {
        updateSnapshots(transaction);
        return value.remove(index);
    }
}
