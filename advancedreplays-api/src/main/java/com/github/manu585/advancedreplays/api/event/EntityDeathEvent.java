package com.github.manu585.advancedreplays.api.event;

/** Records an entity death during recording, including the optional killer. */
public record EntityDeathEvent(
    int tick,
    int entityId,
    int killerId
) implements EntityEvent {

}
