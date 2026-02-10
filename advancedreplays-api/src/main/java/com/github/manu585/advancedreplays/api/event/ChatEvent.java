package com.github.manu585.advancedreplays.api.event;

import java.util.UUID;

/** Records a chat message sent by a player during recording. */
public record ChatEvent(
    int tick,
    UUID senderUuid,
    String senderName,
    String message
) implements ReplayEvent {

}
