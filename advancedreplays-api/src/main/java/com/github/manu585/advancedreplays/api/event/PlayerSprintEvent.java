package com.github.manu585.advancedreplays.api.event;

/** Records a player sprint toggle during recording. */
public record PlayerSprintEvent(
    int tick,
    int entityId,
    boolean sprinting
) implements PlayerActionEvent {

}
