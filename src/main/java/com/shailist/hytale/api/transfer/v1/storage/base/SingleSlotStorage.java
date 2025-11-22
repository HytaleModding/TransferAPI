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

package com.shailist.hytale.api.transfer.v1.storage.base;

import java.util.Iterator;

import com.shailist.hytale.api.transfer.v1.storage.SlottedStorage;
import com.shailist.hytale.api.transfer.v1.storage.StorageView;
import com.shailist.hytale.impl.transfer.TransferApiImpl;
import org.jetbrains.annotations.NotNull;

/**
 * A storage that is also its only storage view.
 * It can be used in APIs for storages that are wrappers around a single "slot", or for slightly more convenient implementation.
 *
 * @param <T> The type of the stored resource.
 */
public interface SingleSlotStorage<T> extends SlottedStorage<T>, StorageView<T> {
	@Override
	default @NotNull Iterator<StorageView<T>> iterator() {
		return TransferApiImpl.singletonIterator(this);
	}

	@Override
	default int getSlotCount() {
		return 1;
	}

	@Override
	default SingleSlotStorage<T> getSlot(int slot) {
		if (slot != 0) {
			throw new IndexOutOfBoundsException("Slot " + slot + " does not exist in a single-slot storage.");
		}

		return this;
	}
}
