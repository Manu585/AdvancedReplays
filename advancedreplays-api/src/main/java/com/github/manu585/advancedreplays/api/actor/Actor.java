package com.github.manu585.advancedreplays.api.actor;

import com.github.manu585.advancedreplays.api.domain.ReplayPosition;

public interface Actor {

  void spawn(ReplayPosition position);

  void teleport(ReplayPosition position);

  void tick();

  void destroy();

}
