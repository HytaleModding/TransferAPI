package com.shailist.TransferAPI;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTransferServiceTest {

    @Test
    void createAndRetrieveTransfer() {
        TransferService svc = new InMemoryTransferService();

        String id = UUID.randomUUID().toString();
        Transfer t = new Transfer(id, 42.0, "from", "to");

        svc.createTransfer(t);

        assertTrue(svc.getTransferById(id).isPresent(), "transfer should be present");
        assertEquals(t, svc.getTransferById(id).get());
        assertEquals(1, svc.listTransfers().size());
    }
}
