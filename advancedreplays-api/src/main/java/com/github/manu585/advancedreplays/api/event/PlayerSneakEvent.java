package com.github.manu585.advancedreplays.api.event;

/** Records a player sneak toggle during recording. */
public record PlayerSneakEvent(
    int tick,
    int entityId,
    boolean sneaking
) implements PlayerActionEvent {

}
