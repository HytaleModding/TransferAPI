package com.shailist.TransferAPI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple in-memory implementation of {@link TransferService} suitable for examples and tests.
 */
public class InMemoryTransferService implements TransferService {
    private final Map<String, Transfer> store = new ConcurrentHashMap<>();

    /**
     * Store the transfer keyed by its id.
     *
     * @param transfer transfer to store
     * @return the stored transfer
     */
    @Override
    public Transfer createTransfer(Transfer transfer) {
        store.put(transfer.getId(), transfer);
        return transfer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Transfer> getTransferById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Transfer> listTransfers() {
        return Collections.unmodifiableList(new ArrayList<>(store.values()));
    }
}
