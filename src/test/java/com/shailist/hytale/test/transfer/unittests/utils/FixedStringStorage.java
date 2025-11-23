package com.shailist.hytale.test.transfer.unittests.utils;

import com.shailist.hytale.api.transfer.v1.storage.StoragePreconditions;
import com.shailist.hytale.api.transfer.v1.storage.TransferVariant;
import com.shailist.hytale.api.transfer.v1.storage.base.FixedVariantStorage;
import com.shailist.hytale.api.transfer.v1.storage.base.SingleVariantStorage;

import java.util.Objects;

/**
 * A storage that can store an allowed string variant or be empty.
 *
 * <p>This is a convenient specialization of {@link FixedVariantStorage} for strings.
 */
public abstract class FixedStringStorage extends FixedVariantStorage<StringVariant> {
    /**
     * Create a fixed string storage with a fixed capacity.
     *
     * @param capacity Fixed capacity of the string storage. Must be non-negative.
     */
    public static FixedStringStorage withFixedCapacity(StringVariant allowedString, long capacity) {
        return withFixedCapacity(allowedString, capacity, () -> {});
    }

    /**
     * Create a fixed string storage with a fixed capacity and a change handler.
     *
     * @param capacity Fixed capacity of the string storage. Must be non-negative.
     * @param onChange Change handler, generally for {@code markDirty()} or similar calls. May not be null.
     */
    public static FixedStringStorage withFixedCapacity(StringVariant allowedString, long capacity, Runnable onChange) {
        StoragePreconditions.notNegative(capacity);
        Objects.requireNonNull(onChange, "onChange may not be null");

        return new FixedStringStorage() {
            @Override
            protected StringVariant getAllowedVariant() {
                return allowedString;
            }

            @Override
            protected long getCapacity(StringVariant variant) {
                return capacity;
            }

            @Override
            protected void onFinalCommit() {
                onChange.run();
            }
        };
    }

    @Override
    protected final StringVariant getBlankVariant() {
        return StringVariant.blank();
    }
}
