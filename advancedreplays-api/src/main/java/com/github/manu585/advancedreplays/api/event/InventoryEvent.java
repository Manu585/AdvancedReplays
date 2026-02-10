package com.github.manu585.advancedreplays.api.event;

import java.util.UUID;

/** Records an inventory-related event during recording. */
public record InventoryEvent(
    int tick,
    UUID playerUuid,
    String inventoryType,
    int slotIndex,
    String action,
    String itemType,
    byte[] itemData
) implements ReplayEvent {

}
