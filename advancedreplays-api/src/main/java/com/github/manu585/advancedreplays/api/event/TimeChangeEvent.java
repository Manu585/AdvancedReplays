package com.github.manu585.advancedreplays.api.event;

/** Records a world time change during recording. */
public record TimeChangeEvent(
    int tick,
    long worldTime
) implements WorldEvent {

}
