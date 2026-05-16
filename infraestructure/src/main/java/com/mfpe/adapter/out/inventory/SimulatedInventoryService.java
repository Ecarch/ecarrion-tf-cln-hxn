package com.mfpe.adapter.out.inventory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mfpe.port.out.InventoryService;
import org.springframework.stereotype.Component;

@Component
public class SimulatedInventoryService implements InventoryService {

    private static final Logger log =
            LoggerFactory.getLogger(SimulatedInventoryService.class);

    @Override
    public boolean isAvailable(String productId, int quantity) {
        log.info("[INVENTORY] Checking availability: {} x{}", productId, quantity);
        return true;
    }
}
