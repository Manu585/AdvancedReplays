package com.github.manu585.advancedreplays.api.event;

/** Records a player arm swing animation during recording. */
public record PlayerSwingEvent(
    int tick,
    int entityId
) implements PlayerActionEvent {

}
