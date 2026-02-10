package com.github.manu585.advancedreplays.api.event;

/** Records an entity equipment change during recording. */
public record EntityEquipmentEvent(
    int tick,
    int entityId,
    String slot,
    String itemType,
    byte[] itemData
) implements EntityEvent {

}
