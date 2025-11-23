package com.shailist.hytale.test.transfer.unittests.utils;

import com.shailist.hytale.api.transfer.v1.transaction.TransactionContext;
import com.shailist.hytale.api.transfer.v1.transaction.base.SnapshotParticipant;
import org.jetbrains.annotations.NotNull;

public class TransactionalString extends SnapshotParticipant<String> {
    private String value;

    public TransactionalString(String startingValue) {
        this.value = startingValue;
    }

    public String get() {
        return this.value;
    }

    public void set(String newValue, @NotNull TransactionContext transaction) {
        updateSnapshots(transaction);
        value = newValue;
    }

    @Override
    protected String createSnapshot() {
        // Save the original value to the side
        var original = value;

        // Set the current value to a copy of the original value
        //noinspection StringOperationCanBeSimplified
        value = new String(original); // Don't mind String being immutable, this is just conceptual

        // Return the original value
        return original;
    }

    @Override
    protected void readSnapshot(String snapshot) {
        value = snapshot;
    }
}
