/*
 * Copyright (c) 2025 Shai List and contributors
 * Licensed under the MIT license. See LICENSE file in the project root for details.
 *
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.shailist.hytale.test.transfer.unittests;

import com.shailist.hytale.test.transfer.unittests.utils.TransactionalString;
import org.junit.jupiter.api.Test;

import com.shailist.hytale.api.transfer.v1.transaction.Transaction;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TransactionStateTests {
    private int callbacksInvoked = 0;

    /**
     * Make sure that transaction global state stays valid in case of exceptions.
     */
    @SuppressWarnings("deprecation")
    @Test
    public void testTransactionExceptions() {
        // Test exception inside the try.
        ensureException(() -> {
            try (Transaction tx = Transaction.openOuter()) {
                tx.addCloseCallback((t, result) -> {
                    callbacksInvoked++; throw new RuntimeException("Close.");
                });
                throw new RuntimeException("Inside try.");
            }
        }, "Exception should have propagated through the transaction.");
        if (callbacksInvoked != 1) throw new AssertionError("Callback should have been invoked.");

        // Test exception inside the close.
        callbacksInvoked = 0;
        ensureException(() -> {
            try (Transaction tx = Transaction.openOuter()) {
                tx.addCloseCallback((t, result) -> {
                    callbacksInvoked++; throw new RuntimeException("Close 1.");
                });
                tx.addCloseCallback((t, result) -> {
                    callbacksInvoked++; throw new RuntimeException("Close 2.");
                });
                tx.addOuterCloseCallback(result -> {
                    callbacksInvoked++; throw new RuntimeException("Outer close 1.");
                });
                tx.addOuterCloseCallback(result -> {
                    callbacksInvoked++; throw new RuntimeException("Outer close 2.");
                });
            }
        }, "Exceptions in close callbacks should be propagated through the transaction.");
        if (callbacksInvoked != 4) throw new AssertionError("All 4 callbacks should have been invoked, only so many were: " + callbacksInvoked);

        // Test getCurrentUnsafe.
        if (Transaction.getCurrentUnsafe() != null) throw new AssertionError("Should have returned null.");

        try (Transaction tx = Transaction.openOuter()) {
            if (Transaction.getCurrentUnsafe() != tx) throw new AssertionError("Should have returned the current transaction.");

            tx.addCloseCallback(((transaction, result) -> {
                ensureException(Transaction::getCurrentUnsafe, "Should have thrown an exception in the close callback.");
            }));

            tx.addOuterCloseCallback((result) -> {
                ensureException(Transaction::getCurrentUnsafe, "Should have thrown an exception in the outer close callback.");
            });
        }

        // Test that transaction state is still OK after these exceptions.
        try (Transaction tx = Transaction.openOuter()) {
            tx.commit();
        }
    }

    private static void ensureException(Runnable runnable, String message) {
        boolean failed = false;

        try {
            runnable.run();
        } catch (Throwable t) {
            failed = true;
        }

        if (!failed) {
            throw new AssertionError(message);
        }
    }

    @Test
    public void testTransactionLifecycle() {
        assertEquals(Transaction.Lifecycle.NONE, Transaction.getLifecycle());

        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(Transaction.Lifecycle.OPEN, Transaction.getLifecycle());

            transaction.addCloseCallback((tx, result) -> {
                assertEquals(Transaction.Lifecycle.CLOSING, Transaction.getLifecycle());
            });

            transaction.addOuterCloseCallback(result -> {
                assertEquals(Transaction.Lifecycle.OUTER_CLOSING, Transaction.getLifecycle());
            });
        }

        assertEquals(Transaction.Lifecycle.NONE, Transaction.getLifecycle());
    }

    @Test
    public void testOpeningTransactionDoesNothing() {
        var str = new TransactionalString("Hello");
        assertEquals("Hello", str.get());

        try (var transaction = Transaction.openOuter()) {
            assertEquals("Hello", str.get());
        }

        assertEquals("Hello", str.get());
    }

    @Test
    public void testAbortingTransactionRevertsValue() {
        var str = new TransactionalString("Hello");

        assertEquals("Hello", str.get());

        try (var transaction = Transaction.openOuter()) {
            str.set("World", transaction);
            assertEquals("World", str.get());

            transaction.abort();
        }

        assertEquals("Hello", str.get());
    }

    @Test
    public void testAbortingTransactionIsImplicit() {
        var str = new TransactionalString("Hello");

        assertEquals("Hello", str.get());

        try (var transaction = Transaction.openOuter()) {
            str.set("World", transaction);
            assertEquals("World", str.get());
        }

        assertEquals("Hello", str.get());
    }

    @Test
    public void testCommittingTransactionChangesValue() {
        var str = new TransactionalString("Hello");

        assertEquals("Hello", str.get());

        try (var transaction = Transaction.openOuter()) {
            str.set("World", transaction);
            assertEquals("World", str.get());

            transaction.commit();
        }

        assertEquals("World", str.get());
    }

    @Test
    public void testOpeningNestedTransactionDoesNothing() {
        var str = new TransactionalString("Hello");
        assertEquals("Hello", str.get());

        try (var transaction = Transaction.openOuter()) {
            try (var nestedTransaction = Transaction.openNested(transaction)) {
                assertEquals("Hello", str.get());
            }

            assertEquals("Hello", str.get());
        }

        assertEquals("Hello", str.get());
    }

    @Test
    public void testAbortingNestedTransactionRevertsValue() {
        var str = new TransactionalString("Hello");

        assertEquals("Hello", str.get());

        try (var transaction = Transaction.openOuter()) {
            try (var nestedTransaction = Transaction.openNested(transaction)) {
                str.set("World", nestedTransaction);

                assertEquals("World", str.get());

                nestedTransaction.abort();
            }

            assertEquals("Hello", str.get());
        }

        assertEquals("Hello", str.get());
    }

    @Test
    public void testCommittingNestedTransactionChangesValueDuringTransaction() {
        var str = new TransactionalString("Hello");

        assertEquals("Hello", str.get());

        try (var transaction = Transaction.openOuter()) {
            try (var nestedTransaction = Transaction.openNested(transaction)) {
                str.set("World", nestedTransaction);

                assertEquals("World", str.get());

                nestedTransaction.commit();
            }

            assertEquals("World", str.get());
        }

        assertEquals("Hello", str.get());
    }

    @Test
    public void testCommittingNestedAndOuterTransactionChangesValue() {
        var str = new TransactionalString("Hello");

        assertEquals("Hello", str.get());

        try (var transaction = Transaction.openOuter()) {
            try (var nestedTransaction = Transaction.openNested(transaction)) {
                str.set("World", nestedTransaction);

                assertEquals("World", str.get());

                nestedTransaction.commit();
            }

            assertEquals("World", str.get());

            transaction.commit();
        }

        assertEquals("World", str.get());
    }
}
