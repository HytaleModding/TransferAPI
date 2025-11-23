package com.shailist.hytale.test.transfer.unittests;

import com.shailist.hytale.api.transfer.v1.storage.StorageUtil;
import com.shailist.hytale.test.transfer.unittests.utils.StringSteamBoiler;
import com.shailist.hytale.test.transfer.unittests.utils.TestStorageUtil;
import org.junit.jupiter.api.Test;

import static com.shailist.hytale.test.transfer.unittests.utils.StringConstants.*;
import static org.junit.jupiter.api.Assertions.*;

public class ComplexStorageTests {
    @Test
    public void testStringSteamBoiler() {
        var boiler = new StringSteamBoiler();

        assertNull(StorageUtil.findStoredResource(boiler.exposedWaterTank));
        assertNull(StorageUtil.findStoredResource(boiler.exposedLavaTank));
        assertNull(StorageUtil.findStoredResource(boiler.exposedSteamTank));

        // Insert water and lava

        assertEquals(UNIT_BUCKET, TestStorageUtil.insert(boiler.exposedWaterTank, WATER, UNIT_BUCKET));
        assertEquals(UNIT_BUCKET, TestStorageUtil.insert(boiler.exposedLavaTank, LAVA, UNIT_BUCKET));

        assertEquals(WATER, StorageUtil.findStoredResource(boiler.exposedWaterTank));
        assertEquals(WATER, boiler.waterTank.variant);
        assertEquals(UNIT_BUCKET, boiler.waterTank.amount);

        assertEquals(LAVA, StorageUtil.findStoredResource(boiler.exposedLavaTank));
        assertEquals(LAVA, boiler.lavaTank.variant);
        assertEquals(UNIT_BUCKET, boiler.lavaTank.amount);

        // Can't interact with exposed interfaces in the wrong way

        assertEquals(0L, TestStorageUtil.extract(boiler.exposedWaterTank, WATER, UNIT_BUCKET));
        assertEquals(0L, TestStorageUtil.extract(boiler.exposedLavaTank, LAVA, UNIT_BUCKET));
        assertEquals(0L, TestStorageUtil.insert(boiler.exposedSteamTank, STEAM, UNIT_BUCKET));

        // Produce steam with maximum I/O

        boiler.produceSteam();
        var estimatedSteamProduced = StringSteamBoiler.WATER_CONSUMPTION * StringSteamBoiler.WATER_TO_STEAM_RATIO;

        assertEquals(WATER, StorageUtil.findStoredResource(boiler.exposedWaterTank));
        assertEquals(WATER, boiler.waterTank.variant);
        assertEquals(UNIT_BUCKET - StringSteamBoiler.WATER_CONSUMPTION, boiler.waterTank.amount);

        assertEquals(LAVA, StorageUtil.findStoredResource(boiler.exposedLavaTank));
        assertEquals(LAVA, boiler.lavaTank.variant);
        assertEquals(UNIT_BUCKET - StringSteamBoiler.LAVA_CONSUMPTION, boiler.lavaTank.amount);

        assertEquals(STEAM, StorageUtil.findStoredResource(boiler.exposedSteamTank));
        assertEquals(STEAM, boiler.steamTank.variant);
        assertEquals(estimatedSteamProduced, boiler.steamTank.amount);

        // Extract steam

        assertEquals(estimatedSteamProduced, TestStorageUtil.extract(boiler.exposedSteamTank, STEAM, Long.MAX_VALUE));
        assertNull(StorageUtil.findStoredResource(boiler.exposedSteamTank));

        // Produce steam with partial amount of water

        boiler.lavaTank.amount = UNIT_BUCKET;
        boiler.waterTank.amount = StringSteamBoiler.WATER_CONSUMPTION / 2L;

        boiler.produceSteam();
        estimatedSteamProduced = (StringSteamBoiler.WATER_CONSUMPTION / 2L) * StringSteamBoiler.WATER_TO_STEAM_RATIO;

        assertNull(StorageUtil.findStoredResource(boiler.exposedWaterTank));

        assertEquals(LAVA, StorageUtil.findStoredResource(boiler.exposedLavaTank));
        assertEquals(LAVA, boiler.lavaTank.variant);
        assertEquals(UNIT_BUCKET - StringSteamBoiler.LAVA_CONSUMPTION, boiler.lavaTank.amount);

        assertEquals(STEAM, StorageUtil.findStoredResource(boiler.exposedSteamTank));
        assertEquals(STEAM, boiler.steamTank.variant);
        assertEquals(estimatedSteamProduced, boiler.steamTank.amount);

        // Add water back into the tank, just for ease of use sake
        assertEquals(UNIT_BUCKET, TestStorageUtil.insert(boiler.exposedWaterTank, WATER, UNIT_BUCKET));

        // Produce steam with near full output tank

        boiler.lavaTank.amount = StringSteamBoiler.LAVA_CAPACITY;
        boiler.waterTank.amount = StringSteamBoiler.WATER_CAPACITY;
        boiler.steamTank.amount = StringSteamBoiler.STEAM_CAPACITY - 1L;

        boiler.produceSteam();

        assertEquals(WATER, StorageUtil.findStoredResource(boiler.exposedWaterTank));
        assertEquals(WATER, boiler.waterTank.variant);
        assertEquals(StringSteamBoiler.WATER_CAPACITY - StringSteamBoiler.WATER_CONSUMPTION, boiler.waterTank.amount);

        assertEquals(LAVA, StorageUtil.findStoredResource(boiler.exposedLavaTank));
        assertEquals(LAVA, boiler.lavaTank.variant);
        assertEquals(StringSteamBoiler.LAVA_CAPACITY - StringSteamBoiler.LAVA_CONSUMPTION, boiler.lavaTank.amount);

        assertEquals(STEAM, StorageUtil.findStoredResource(boiler.exposedSteamTank));
        assertEquals(STEAM, boiler.steamTank.variant);
        assertEquals(StringSteamBoiler.STEAM_CAPACITY, boiler.steamTank.amount);

        // Produce steam with near full output

        boiler.lavaTank.amount = StringSteamBoiler.LAVA_CAPACITY;
        boiler.waterTank.amount = StringSteamBoiler.WATER_CAPACITY;
        boiler.steamTank.amount = StringSteamBoiler.STEAM_CAPACITY;

        boiler.produceSteam();

        assertEquals(WATER, StorageUtil.findStoredResource(boiler.exposedWaterTank));
        assertEquals(WATER, boiler.waterTank.variant);
        assertEquals(StringSteamBoiler.WATER_CAPACITY - StringSteamBoiler.WATER_CONSUMPTION, boiler.waterTank.amount);

        assertEquals(LAVA, StorageUtil.findStoredResource(boiler.exposedLavaTank));
        assertEquals(LAVA, boiler.lavaTank.variant);
        assertEquals(StringSteamBoiler.LAVA_CAPACITY - StringSteamBoiler.LAVA_CONSUMPTION, boiler.lavaTank.amount);

        assertEquals(STEAM, StorageUtil.findStoredResource(boiler.exposedSteamTank));
        assertEquals(STEAM, boiler.steamTank.variant);
        assertEquals(StringSteamBoiler.STEAM_CAPACITY, boiler.steamTank.amount);
    }
}
