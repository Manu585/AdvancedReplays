package com.github.manu585.advancedreplays.api.event;

import com.github.manu585.advancedreplays.api.domain.ReplayPosition;

/** Records an entity position and rotation change during recording. */
public record EntityMoveEvent(
    int tick,
    int entityId,
    ReplayPosition position,
    boolean onGround
) implements EntityEvent {

}
