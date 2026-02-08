package com.github.manu585.advancedreplays.api.actor.specs;

import com.github.manu585.advancedreplays.api.actor.ActorType;

public record MobSpec(ActorType actorType) implements ActorSpec {

  public MobSpec {
    if (actorType == ActorType.PLAYER) {
      throw new IllegalArgumentException("MobSpec cannot be PLAYER!");
    }
  }

}
