package com.github.manu585.advancedreplays.api.actor;

import com.github.manu585.advancedreplays.api.domain.ReplayPosition;

/** Represents a client-side replay actor that can be spawned, moved, and destroyed via packets. */
public interface Actor {

  void spawn(ReplayPosition position);

  void teleport(ReplayPosition position);

  void tick();

  void destroy();

}
