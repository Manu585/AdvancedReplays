package com.github.manu585.advancedreplays.core.entity;

import com.github.manu585.advancedreplays.core.domain.Position;

public interface Actor {

  String displayName();

  Position position();

  void tick();

}
