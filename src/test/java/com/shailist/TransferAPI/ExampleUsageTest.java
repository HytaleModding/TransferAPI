package com.shailist.TransferAPI;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ExampleUsageTest {

    @Test
    void runDemoDoesNotThrow() {
        assertDoesNotThrow(ExampleUsage::runDemo);
    }
}
