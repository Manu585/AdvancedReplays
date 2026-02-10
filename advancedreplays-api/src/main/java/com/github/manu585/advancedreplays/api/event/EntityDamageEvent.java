package com.github.manu585.advancedreplays.api.event;

/** Records an entity taking damage during recording. */
public record EntityDamageEvent(
    int tick,
    int entityId,
    int attackerId,
    double damage,
    String damageSource
) implements EntityEvent {

}
