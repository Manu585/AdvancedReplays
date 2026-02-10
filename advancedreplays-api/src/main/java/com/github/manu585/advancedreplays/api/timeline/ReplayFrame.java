package com.github.manu585.advancedreplays.api.timeline;

import com.github.manu585.advancedreplays.api.event.ReplayEvent;

import java.util.List;

/** A single frame in a replay timeline, grouping all events that occurred at a given tick. */
public record ReplayFrame(int tick, List<ReplayEvent> events) {

}
