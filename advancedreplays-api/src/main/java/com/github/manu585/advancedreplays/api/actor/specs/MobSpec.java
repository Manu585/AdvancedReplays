package com.github.manu585.advancedreplays.api.actor.specs;

import com.github.manu585.advancedreplays.api.actor.ActorType;

/** Specification for creating a mob-type replay actor with a given entity type. */
public record MobSpec(ActorType actorType) implements ActorSpec {

  public MobSpec {
    if (actorType == ActorType.PLAYER) {
      throw new IllegalArgumentException("MobSpec cannot be PLAYER!");
    }
  }

}
