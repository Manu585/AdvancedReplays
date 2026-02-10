package com.github.manu585.advancedreplays.api.timeline;

import java.util.List;
import java.util.Map;

/** A keyframe capturing full world and entity state at a specific tick for seek support. */
public record Keyframe(
    int tick,
    List<EntitySnapshot> entityStates,
    Map<String, String> blockOverrides,
    WorldStateSnapshot worldState,
    long byteOffset
) {

}
