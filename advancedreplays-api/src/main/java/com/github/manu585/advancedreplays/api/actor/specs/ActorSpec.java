package com.github.manu585.advancedreplays.api.actor.specs;

import com.github.manu585.advancedreplays.api.actor.ActorType;

/** Sealed specification describing what kind of actor to create. */
public sealed interface ActorSpec permits PlayerSpec, MobSpec {

  ActorType actorType();

}
