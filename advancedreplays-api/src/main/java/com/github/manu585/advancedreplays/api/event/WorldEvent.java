package com.github.manu585.advancedreplays.api.event;

/** Marker interface for replay events related to world state changes. */
public sealed interface WorldEvent extends ReplayEvent permits
    WeatherChangeEvent,
    TimeChangeEvent {

}
