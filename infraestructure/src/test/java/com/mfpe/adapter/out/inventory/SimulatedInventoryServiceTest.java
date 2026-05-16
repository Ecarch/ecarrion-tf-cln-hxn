package com.mfpe.adapter.out.inventory;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SimulatedInventoryServiceTest {

    private final SimulatedInventoryService service = new SimulatedInventoryService();

    @Test
    void isAvailable_shouldAlwaysReturnTrue() {
        assertTrue(service.isAvailable("PROD-1", 10));
    }

    @Test
    void isAvailable_shouldReturnTrueForZeroQuantity() {
        assertTrue(service.isAvailable("PROD-2", 0));
    }

    @Test
    void isAvailable_shouldReturnTrueForAnyProduct() {
        assertTrue(service.isAvailable("ANY-PRODUCT", 999));
    }
}
