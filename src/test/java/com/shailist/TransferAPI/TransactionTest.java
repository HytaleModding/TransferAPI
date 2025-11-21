/*
 * Copyright (c) 2025 Shai List and contributors
 * Licensed under the MIT license. See LICENSE file in the project root for details.
 */

package com.shailist.TransferAPI;

import com.shailist.TransferAPI.api.transfer.v1.transaction.Transaction;
import com.shailist.TransferAPI.api.transfer.v1.transaction.TransactionContext;
import com.shailist.TransferAPI.api.transfer.v1.transaction.base.SnapshotParticipant;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TransactionTest {
    public static class TransactionalString extends SnapshotParticipant<String> {
        private String value;

        public TransactionalString(String startingValue) {
            this.value = startingValue;
        }

        public String get() {
            return this.value;
        }

        public void set(String newValue, TransactionContext transaction) {
            updateSnapshots(transaction);
            value = newValue;
        }

        @Override
        protected String createSnapshot() {
            var original = value;
            value = new String(original); // Don't mind String being immutable, this is just conceptual
            return original;
        }

        @Override
        protected void readSnapshot(String snapshot) {
            value = snapshot;
        }
    }

    @Test
    public void testOpeningTransactionDoesNothing() {
        var str = new TransactionalString("Hello");
        assertEquals(str.get(), "Hello");

        try (var transaction = Transaction.openOuter()) {
            assertEquals(str.get(), "Hello");
        }

        assertEquals(str.get(), "Hello");
    }

    @Test
    public void testAbortingTransactionRevertsValue() {
        var str = new TransactionalString("Hello");

        assertEquals(str.get(), "Hello");

        try (var transaction = Transaction.openOuter()) {
            str.set("World", transaction);
            assertEquals(str.get(), "World");

            transaction.abort();
        }

        assertEquals(str.get(), "Hello");
    }

    @Test
    public void testAbortingTransactionIsImplicit() {
        var str = new TransactionalString("Hello");

        assertEquals(str.get(), "Hello");

        try (var transaction = Transaction.openOuter()) {
            str.set("World", transaction);
            assertEquals(str.get(), "World");
        }

        assertEquals(str.get(), "Hello");
    }

    @Test
    public void testCommittingTransactionChangesValue() {
        var str = new TransactionalString("Hello");

        assertEquals(str.get(), "Hello");

        try (var transaction = Transaction.openOuter()) {
            str.set("World", transaction);
            assertEquals(str.get(), "World");

            transaction.commit();
        }

        assertEquals(str.get(), "World");
    }

    @Test
    public void testOpeningNestedTransactionDoesNothing() {
        var str = new TransactionalString("Hello");
        assertEquals(str.get(), "Hello");

        try (var transaction = Transaction.openOuter()) {
            try (var nestedTransaction = Transaction.openNested(transaction)) {
                assertEquals(str.get(), "Hello");
            }

            assertEquals(str.get(), "Hello");
        }

        assertEquals(str.get(), "Hello");
    }

    @Test
    public void testAbortingNestedTransactionRevertsValue() {
        var str = new TransactionalString("Hello");

        assertEquals(str.get(), "Hello");

        try (var transaction = Transaction.openOuter()) {
            try (var nestedTransaction = Transaction.openNested(transaction)) {
                str.set("World", nestedTransaction);

                assertEquals(str.get(), "World");

                nestedTransaction.abort();
            }

            assertEquals(str.get(), "Hello");
        }

        assertEquals(str.get(), "Hello");
    }

    @Test
    public void testCommittingNestedTransactionChangesValueDuringTransaction() {
        var str = new TransactionalString("Hello");

        assertEquals(str.get(), "Hello");

        try (var transaction = Transaction.openOuter()) {
            try (var nestedTransaction = Transaction.openNested(transaction)) {
                str.set("World", nestedTransaction);

                assertEquals(str.get(), "World");

                nestedTransaction.commit();
            }

            assertEquals(str.get(), "World");
        }

        assertEquals(str.get(), "Hello");
    }

    @Test
    public void testCommittingNestedAndOuterTransactionChangesValue() {
        var str = new TransactionalString("Hello");

        assertEquals(str.get(), "Hello");

        try (var transaction = Transaction.openOuter()) {
            try (var nestedTransaction = Transaction.openNested(transaction)) {
                str.set("World", nestedTransaction);

                assertEquals(str.get(), "World");

                nestedTransaction.commit();
            }

            assertEquals(str.get(), "World");

            transaction.commit();
        }

        assertEquals(str.get(), "World");
    }
}
