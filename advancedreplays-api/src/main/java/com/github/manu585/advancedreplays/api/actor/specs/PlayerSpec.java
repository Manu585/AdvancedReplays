package com.github.manu585.advancedreplays.api.actor.specs;

import com.github.manu585.advancedreplays.api.actor.ActorType;
import com.github.manu585.advancedreplays.api.domain.ActorProfile;

/** Specification for creating a player-type replay actor with profile and skin data. */
public record PlayerSpec(ActorProfile actorProfile) implements ActorSpec {

  @Override
  public ActorType actorType() {
    return ActorType.PLAYER;
  }

}
