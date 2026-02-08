package com.github.manu585.advancedreplays.api.actor.specs;

import com.github.manu585.advancedreplays.api.actor.ActorType;
import com.github.manu585.advancedreplays.api.domain.ActorProfile;

public record PlayerSpec(ActorProfile actorProfile) implements ActorSpec {

  @Override
  public ActorType actorType() {
    return ActorType.PLAYER;
  }

}
