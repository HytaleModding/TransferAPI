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

package com.shailist.TransferAPI.api.transfer.v1.storage.base;

import java.util.List;
import java.util.StringJoiner;

import com.shailist.TransferAPI.api.transfer.v1.storage.SlottedStorage;
import com.shailist.TransferAPI.api.transfer.v1.storage.Storage;

/**
 * A {@link SlottedStorage} composed from multiple underlying slotted storages.
 *
 * <p>The slots of the component storages are concatenated, so slot indices are translated
 * into the appropriate component storage and slot within that storage.
 *
 * @param <T> the resource type
 * @param <S> the concrete slotted storage type for each part
 */
public class CombinedSlottedStorage<T, S extends SlottedStorage<T>> extends CombinedStorage<T, S> implements SlottedStorage<T> {
	/**
	 * Create a combined slotted storage from the provided parts.
	 *
	 * @param parts the list of slotted storages to combine
	 */
	public CombinedSlottedStorage(List<S> parts) {
		super(parts);
	}

	@Override
	public int getSlotCount() {
		int count = 0;

		for (S part : parts) {
			count += part.getSlotCount();
		}

		return count;
	}

	@Override
	public SingleSlotStorage<T> getSlot(int slot) {
		int updatedSlot = slot;

		for (SlottedStorage<T> part : parts) {
			if (updatedSlot < part.getSlotCount()) {
				return part.getSlot(updatedSlot);
			}

			updatedSlot -= part.getSlotCount();
		}

		throw new IndexOutOfBoundsException("Slot " + slot + " is out of bounds. This storage has size " + getSlotCount());
	}

	@Override
	public String toString() {
		StringJoiner partNames = new StringJoiner(", ");

		for (S part : parts) {
			partNames.add(part.toString());
		}

		return "CombinedSlottedStorage[" + partNames + "]";
	}
}
