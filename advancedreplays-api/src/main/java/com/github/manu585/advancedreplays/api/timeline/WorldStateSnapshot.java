package com.github.manu585.advancedreplays.api.timeline;

/** Snapshot of world-level state (weather, time) at a particular tick. */
public record WorldStateSnapshot(
    boolean raining,
    boolean thundering,
    long worldTime
) {

}
