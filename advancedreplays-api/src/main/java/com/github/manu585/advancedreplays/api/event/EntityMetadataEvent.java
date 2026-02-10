package com.github.manu585.advancedreplays.api.event;

import java.util.Map;

/** Records an entity metadata change during recording. */
public record EntityMetadataEvent(
    int tick,
    int entityId,
    Map<String, String> metadataSnapshot
) implements EntityEvent {

}
