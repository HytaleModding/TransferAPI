package dev.hytalemodding.test.transfer.unittests.utils;

import dev.hytalemodding.api.transfer.v1.storage.Storage;
import dev.hytalemodding.api.transfer.v1.storage.StorageView;
import dev.hytalemodding.api.transfer.v1.transaction.Transaction;
import dev.hytalemodding.api.transfer.v1.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public final class TestStorageUtil {
    public static <T> long insert(Storage<T> storage, T resource, long maxAmount) {
        return insert(storage, resource, maxAmount, null);
    }

    public static <T> long insert(Storage<T> storage, T resource, long maxAmount, @Nullable TransactionContext transaction) {
        try (Transaction nestedTransaction = Transaction.openNested(transaction)) {
            var result = storage.insert(resource, maxAmount, nestedTransaction);
            nestedTransaction.commit();
            return result;
        }
    }

    public static <T> long extract(Storage<T> storage, T resource, long maxAmount) {
        return extract(storage, resource, maxAmount, null);
    }

    public static <T> long extract(Storage<T> storage, T resource, long maxAmount, @Nullable TransactionContext transaction) {
        try (Transaction nestedTransaction = Transaction.openNested(transaction)) {
            var result = storage.extract(resource, maxAmount, nestedTransaction);
            nestedTransaction.commit();
            return result;
        }
    }

    public static <T> long extract(StorageView<T> storageView, T resource, long maxAmount) {
        return extract(storageView, resource, maxAmount, null);
    }

    public static <T> long extract(StorageView<T> storageView, T resource, long maxAmount, @Nullable TransactionContext transaction) {
        try (Transaction nestedTransaction = Transaction.openNested(transaction)) {
            var result = storageView.extract(resource, maxAmount, nestedTransaction);
            nestedTransaction.commit();
            return result;
        }
    }

    public static <T, S extends Object & Storage<T> & StorageView<T>> long extract(S storage, T resource, long maxAmount) {
        return extract(storage, resource, maxAmount, null);
    }

    public static <T, S extends Object & Storage<T> & StorageView<T>> long extract(S storage, T resource, long maxAmount, @Nullable TransactionContext transaction) {
        try (Transaction nestedTransaction = Transaction.openNested(transaction)) {
            var result = storage.extract(resource, maxAmount, nestedTransaction);
            nestedTransaction.commit();
            return result;
        }
    }
}
