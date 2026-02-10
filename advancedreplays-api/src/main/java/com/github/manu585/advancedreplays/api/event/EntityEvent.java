package com.github.manu585.advancedreplays.api.event;

/** Marker interface for replay events related to entities. */
public sealed interface EntityEvent extends ReplayEvent permits
    EntitySpawnEvent,
    EntityDeathEvent,
    EntityMoveEvent,
    EntityEquipmentEvent,
    EntityDamageEvent,
    EntityMetadataEvent {

}
