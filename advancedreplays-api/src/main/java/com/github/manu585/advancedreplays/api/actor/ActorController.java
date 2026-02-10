package com.github.manu585.advancedreplays.api.actor;

import com.github.manu585.advancedreplays.api.event.ReplayEvent;
import com.github.manu585.advancedreplays.api.timeline.EntitySnapshot;

/** High-level controller for managing replay actors during playback. */
public interface ActorController {

  Actor actor();

  int replayEntityId();

  void applyEvent(ReplayEvent event);

  void applySnapshot(EntitySnapshot snapshot);

}
