package com.github.manu585.advancedreplays.api.event;

/** Records a generic player action during recording. */
public sealed interface PlayerActionEvent extends ReplayEvent permits
    PlayerSwingEvent,
    PlayerSneakEvent,
    PlayerSprintEvent {

}
