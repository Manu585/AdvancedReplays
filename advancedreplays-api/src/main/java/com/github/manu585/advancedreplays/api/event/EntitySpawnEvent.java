package com.github.manu585.advancedreplays.api.event;

import com.github.manu585.advancedreplays.api.domain.ActorProfile;
import com.github.manu585.advancedreplays.api.domain.ReplayPosition;

/** Records an entity spawning into the world during recording. */
public record EntitySpawnEvent(
    int tick,
    int entityId,
    String entityType,
    ReplayPosition position,
    ActorProfile profile
) implements EntityEvent {

}
