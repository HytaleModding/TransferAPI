package com.shailist.hytale.test.transfer.unittests.utils;

import com.shailist.hytale.api.transfer.v1.storage.Storage;
import com.shailist.hytale.api.transfer.v1.storage.base.FilteringStorage;
import com.shailist.hytale.api.transfer.v1.transaction.Transaction;

import static com.shailist.hytale.test.transfer.unittests.utils.StringConstants.*;

/**
 * A class that conceptually implements a "steam boiler".
 * It contains 2 input tanks of "Lava" and "Water", and 1 output tank for "Steam".
 * It contains a {@link #produceSteam()} method that takes {@link #LAVA_CONSUMPTION} lava and up to
 * {@link #WATER_CONSUMPTION} water, and produces {@link #WATER_TO_STEAM_RATIO} times the extracted water amount of
 * steam.
 */
public class StringSteamBoiler {
    // Constants
    public static final long WATER_CONSUMPTION = 100L * UNIT_MILLIBUCKET;
    public static final long LAVA_CONSUMPTION = UNIT_MILLIBUCKET;
    public static final long WATER_TO_STEAM_RATIO = 20L;

    public static final long WATER_CAPACITY = 8L * UNIT_BUCKET;
    public static final long LAVA_CAPACITY = 8L * UNIT_BUCKET;
    public static final long STEAM_CAPACITY = 8L * UNIT_BUCKET;

    // Internal tanks - support both insertion and extraction, public for the sake of testing
    public final FixedStringStorage lavaTank = FixedStringStorage.withFixedCapacity(LAVA, WATER_CAPACITY);
    public final FixedStringStorage waterTank = FixedStringStorage.withFixedCapacity(WATER, LAVA_CAPACITY);
    public final FixedStringStorage steamTank = FixedStringStorage.withFixedCapacity(STEAM, STEAM_CAPACITY);

    // Exposed tanks - can be interacted by the outside world
    public final Storage<StringVariant> exposedLavaTank = FilteringStorage.insertOnlyOf(lavaTank);
    public final Storage<StringVariant> exposedWaterTank = FilteringStorage.insertOnlyOf(waterTank);
    public final Storage<StringVariant> exposedSteamTank = FilteringStorage.extractOnlyOf(steamTank);

    public void produceSteam() {
        try (Transaction transaction = Transaction.openOuter()) {
            long waterExtracted = waterTank.extract(WATER_CONSUMPTION, transaction);
            if (waterExtracted == 0) return; // aborts the transaction

            long lavaExtracted = lavaTank.extract(LAVA_CONSUMPTION, transaction);
            if (lavaExtracted != LAVA_CONSUMPTION) return;  // aborts the transaction

            long steamProduced = waterExtracted * WATER_TO_STEAM_RATIO;
            steamTank.insert(steamProduced, transaction); // ignore how much was inserted, we don't care

            transaction.commit();
        }
    }
}
