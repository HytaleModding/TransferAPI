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

import java.util.Iterator;

import com.shailist.hytale.api.transfer.v1.storage.base.FixedVariantStorage;
import com.shailist.hytale.api.transfer.v1.storage.base.ResourceAmount;
import com.shailist.hytale.test.transfer.unittests.utils.*;
import org.junit.jupiter.api.Test;

import com.shailist.hytale.api.transfer.v1.storage.Storage;
import com.shailist.hytale.api.transfer.v1.storage.StorageUtil;
import com.shailist.hytale.api.transfer.v1.storage.StorageView;
import com.shailist.hytale.api.transfer.v1.storage.base.FilteringStorage;
import com.shailist.hytale.api.transfer.v1.storage.base.SingleVariantStorage;
import com.shailist.hytale.api.transfer.v1.transaction.Transaction;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BaseStorageTests {

    @Test
    public void testSingleVariantStorage() {
        SingleVariantStorage<StringVariant> storage = SingleStringStorage.withFixedCapacity(10L * StringConstants.UNIT);

        StringVariant hello = StringVariant.of(StringConstants.HELLO);
        StringVariant world = StringVariant.of(StringConstants.WORLD);

        assertEquals(0L, storage.amount);
        assertEquals(StringVariant.blank(), storage.variant);

        // Insertion into an empty storage should succeed.
        assertEquals(StringConstants.UNIT, TestStorageUtil.insert(storage, hello, StringConstants.UNIT));
        assertEquals(StringConstants.UNIT, storage.amount);
        assertEquals(hello, storage.variant);

        // The string should be visible.
        assertEquals(hello, StorageUtil.findStoredResource(storage));
        assertNull(StorageUtil.findStoredResource(storage, fv -> fv.isOf(StringConstants.WORLD)));

        assertEquals(hello, StorageUtil.findExtractableResource(storage, null));
        assertEquals(new ResourceAmount<>(hello, StringConstants.UNIT), StorageUtil.findExtractableContent(storage, null));

        // Insertion into a non-empty storage with the same variant should succeed.
        assertEquals(StringConstants.UNIT, TestStorageUtil.insert(storage, hello, StringConstants.UNIT));
        assertEquals(2 * StringConstants.UNIT, storage.amount);
        assertEquals(hello, storage.variant);

        // Insertion into a non-empty storage with a different variant should fail.
        assertEquals(0L, TestStorageUtil.insert(storage, world, StringConstants.UNIT));
        assertEquals(2 * StringConstants.UNIT, storage.amount);
        assertEquals(hello, storage.variant);

        // Extraction from a non-empty storage with the same variant should succeed.
        assertEquals(StringConstants.UNIT, TestStorageUtil.extract(storage, hello, StringConstants.UNIT));
        assertEquals(StringConstants.UNIT, storage.amount);
        assertEquals(hello, storage.variant);

        // Extraction from a non-empty storage with a different variant should fail.
        assertEquals(0L, TestStorageUtil.extract(storage, world, StringConstants.UNIT));
        assertEquals(StringConstants.UNIT, storage.amount);
        assertEquals(hello, storage.variant);

        // Empty the storage for the next test
        storage.amount = 0L;
        storage.variant = StringVariant.blank();

        // Extraction from an empty storage should fail.
        assertEquals(0L, TestStorageUtil.extract(storage, hello, StringConstants.UNIT));
        assertEquals(0, storage.amount);
        assertEquals(StringVariant.blank(), storage.variant);
    }

	@Test
	public void testFilteringStorage() {
        SingleVariantStorage<StringVariant> storage = SingleStringStorage.withFixedCapacity(10L * StringConstants.UNIT);
		Storage<StringVariant> noHello = new FilteringStorage<>(storage) {
			@Override
			protected boolean canExtract(StringVariant resource) {
				return !resource.isOf(StringConstants.HELLO);
			}

			@Override
			protected boolean canInsert(StringVariant resource) {
				return !resource.isOf(StringConstants.HELLO);
			}
		};
		StringVariant hello = StringVariant.of(StringConstants.HELLO);
		StringVariant world = StringVariant.of(StringConstants.WORLD);

		// Inserting a non filtered resource should fail.
		assertEquals(0L, TestStorageUtil.insert(noHello, hello, StringConstants.UNIT));
        // Inserting a filtered resource should succeed.
		assertEquals(StringConstants.UNIT, TestStorageUtil.insert(noHello, world, StringConstants.UNIT));

        // Filtering storage should find filtered stored resource
        assertEquals(world, StorageUtil.findStoredResource(noHello));
        assertEquals(world, StorageUtil.findStoredResource(noHello, fv -> fv.isOf(StringConstants.WORLD)));

        assertEquals(world, StorageUtil.findExtractableResource(noHello, null));
        assertEquals(new ResourceAmount<>(world, StringConstants.UNIT), StorageUtil.findExtractableContent(noHello, null));

		// Extracting a non filtered resource should fail.
		assertEquals(0L, TestStorageUtil.extract(noHello, hello, StringConstants.UNIT));
        // Extracting filtered resource should succeed.
		assertEquals(StringConstants.UNIT, TestStorageUtil.extract(noHello, world, StringConstants.UNIT));

        // Adding non-filtered resource to the underlying storage for some tests
        assertEquals(StringConstants.UNIT, TestStorageUtil.insert(storage, hello, StringConstants.UNIT));

        // Filtering storage should not extract non-filtered stored resource
        assertEquals(0L, TestStorageUtil.extract(noHello, hello, StringConstants.UNIT));
        // Filtering storage should find non-filtered stored resource
        assertEquals(hello, StorageUtil.findStoredResource(noHello));
        assertEquals(hello, StorageUtil.findStoredResource(noHello, fv -> fv.isOf(StringConstants.HELLO)));
        // Filtering storage should not find non-filtered extractable stored resource
        assertNull(StorageUtil.findExtractableResource(noHello, null));
        assertNull(StorageUtil.findExtractableContent(noHello, null));
        // Inserting filtered resource into filtered storage should return the result of inserting into the underlying
        // storage, which should fail in this case
        assertEquals(0L, TestStorageUtil.insert(noHello, world, StringConstants.UNIT));
        // Clear the underlying storage
        assertEquals(StringConstants.UNIT, TestStorageUtil.extract(storage, hello, StringConstants.UNIT));
	}

	/**
	 * Regression test for <a href="https://github.com/FabricMC/fabric/issues/3414">
	 * {@code nonEmptyIterator} not handling views that become empty during iteration correctly</a>.
	 */
	@Test
	public void testNonEmptyIteratorWithModifiedView() {
		SingleVariantStorage<StringVariant> storage = SingleStringStorage.withFixedCapacity(StringConstants.UNIT, () -> { });
		storage.variant = StringVariant.of(StringConstants.HELLO);

		Iterator<StorageView<StringVariant>> iterator = storage.nonEmptyIterator();
		storage.amount = StringConstants.UNIT;
		// Iterator should have a next element now
        assertTrue(iterator.hasNext());
		assertEquals(storage, iterator.next());

		iterator = storage.nonEmptyIterator();
		storage.amount = 0;
		// Iterator should not have a next element...
        assertFalse(iterator.hasNext());
	}

    @Test
    public void testFixedVariantStorage() {
        StringVariant hello = StringVariant.of(StringConstants.HELLO);
        StringVariant world = StringVariant.of(StringConstants.WORLD);

        FixedVariantStorage<StringVariant> storage = FixedStringStorage.withFixedCapacity(hello, 10L * StringConstants.UNIT);

        assertEquals(0L, storage.amount);
        assertEquals(StringVariant.blank(), storage.variant);

        // Insertion of a non-allowed variant into an empty storage should fail.
        assertEquals(0L, TestStorageUtil.insert(storage, world, StringConstants.UNIT));

        // Insertion of allowed variant into an empty storage should succeed.
        assertEquals(StringConstants.UNIT, TestStorageUtil.insert(storage, hello, StringConstants.UNIT));

        // Insertion of a non-allowed variant into a non-empty storage should fail.
        assertEquals(0L, TestStorageUtil.insert(storage, world, StringConstants.UNIT));

        // Extraction from a non-empty storage with the allowed variant should succeed.
        assertEquals(StringConstants.UNIT, TestStorageUtil.extract(storage, hello, StringConstants.UNIT));
    }
}
