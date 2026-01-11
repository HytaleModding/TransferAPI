package dev.hytalemodding.test.transfer.unittests.utils;

import dev.hytalemodding.api.transfer.v1.storage.StoragePreconditions;
import dev.hytalemodding.api.transfer.v1.storage.base.SingleVariantStorage;

import java.util.Objects;

/**
 * A storage that can store a single string variant at any given time.
 *
 * <p>This is a convenient specialization of {@link SingleVariantStorage} for strings.
 */
public abstract class SingleStringStorage extends SingleVariantStorage<StringVariant> {
    /**
     * Create a string storage with a fixed capacity.
     *
     * @param capacity Fixed capacity of the string storage. Must be non-negative.
     */
    public static SingleStringStorage withFixedCapacity(long capacity) {
        return withFixedCapacity(capacity, () -> {});
    }

    /**
     * Create a string storage with a fixed capacity and a change handler.
     *
     * @param capacity Fixed capacity of the string storage. Must be non-negative.
     * @param onChange Change handler, generally for {@code markDirty()} or similar calls. May not be null.
     */
    public static SingleStringStorage withFixedCapacity(long capacity, Runnable onChange) {
        StoragePreconditions.notNegative(capacity);
        Objects.requireNonNull(onChange, "onChange may not be null");

        return new SingleStringStorage() {
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

