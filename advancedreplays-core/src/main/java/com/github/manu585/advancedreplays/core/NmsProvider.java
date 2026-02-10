package com.github.manu585.advancedreplays.core;

import com.github.manu585.advancedreplays.api.actor.ActorFactory;
import com.github.manu585.advancedreplays.core.playback.WorldEffectHandler;

/** Bridge interface for obtaining version-specific NMS implementations via reflection. */
public interface NmsProvider {

  ActorFactory createActorFactory();

  WorldEffectHandler createWorldEffectHandler();

}
