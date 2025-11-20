package com.shailist.TransferAPI;

import java.util.UUID;

/**
 * Small example demonstrating usage of the library.
 */
public final class ExampleUsage {
    private ExampleUsage() {}

    /**
     * Run a tiny demo which creates two transfers and prints them.
     */
    public static void runDemo() {
        TransferService service = new InMemoryTransferService();

        Transfer t1 = new Transfer(UUID.randomUUID().toString(), 100.0, "acct-A", "acct-B");
        Transfer t2 = new Transfer(UUID.randomUUID().toString(), 250.5, "acct-C", "acct-D");

        service.createTransfer(t1);
        service.createTransfer(t2);

        System.out.println("Created transfers:");
        service.listTransfers().forEach(System.out::println);

        service.getTransferById(t1.getId()).ifPresent(tr -> System.out.println("Found: " + tr));
    }
}
