package com.github.manu585.advancedreplays.api.event;

import com.github.manu585.advancedreplays.api.domain.BlockPos;

/** Records a block state change, including the block that was placed or broken. */
public record BlockChangeEvent(
    int tick,
    BlockPos blockPos,
    String previousType,
    String newType,
    int actorId,
    BlockChangeCause cause
) implements BlockEvent {

}
