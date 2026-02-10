package com.github.manu585.advancedreplays.api.timeline;

import com.github.manu585.advancedreplays.api.domain.ActorProfile;
import com.github.manu585.advancedreplays.api.domain.ReplayPosition;

import java.util.Map;

/** Snapshot of an entity's state at a particular tick, used in keyframes. */
public record EntitySnapshot(
    int entityId,
    String entityType,
    ReplayPosition position,
    ActorProfile profile,
    Map<String, byte[]> equipmentData,
    Map<String, String> metadataSnapshot
) {

}
