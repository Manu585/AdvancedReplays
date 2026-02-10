package com.github.manu585.advancedreplays.api.event;

/** Records a weather state change during recording. */
public record WeatherChangeEvent(
    int tick,
    boolean raining,
    boolean thundering
) implements WorldEvent {

}
