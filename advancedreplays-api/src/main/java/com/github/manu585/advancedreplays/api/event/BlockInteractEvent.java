package com.github.manu585.advancedreplays.api.event;

import com.github.manu585.advancedreplays.api.domain.BlockPos;

/** Records a player interaction with a block, such as right-clicking. */
public record BlockInteractEvent(
    int tick,
    BlockPos blockPos,
    int actorId,
    String interactionType
) implements BlockEvent {

}
