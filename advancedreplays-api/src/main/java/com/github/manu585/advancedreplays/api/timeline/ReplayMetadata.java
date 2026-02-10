package com.github.manu585.advancedreplays.api.timeline;

import java.time.Instant;
import java.util.UUID;

/** Metadata describing a replay recording, including player info, timing, and session details. */
public record ReplayMetadata(
    UUID replayId,
    String worldName,
    Instant startTimestamp,
    int durationTicks,
    String serverVersion,
    int protocolVersion,
    String origin,
    UUID playerUuid,
    String playerName,
    UUID sessionId,
    int partIndex
) {

}
