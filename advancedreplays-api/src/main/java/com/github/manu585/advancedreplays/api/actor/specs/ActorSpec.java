package com.github.manu585.advancedreplays.api.actor.specs;

import com.github.manu585.advancedreplays.api.actor.ActorType;

public sealed interface ActorSpec permits PlayerSpec, MobSpec {

  ActorType actorType();

}
