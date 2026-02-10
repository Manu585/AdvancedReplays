package com.github.manu585.advancedreplays.core.playback;

import com.github.manu585.advancedreplays.api.event.*;

/** Dispatches recorded replay events to the appropriate actor or world effect handlers during playback. */
public final class EventDispatcher {

  private final ActorManager actorManager;
  private final WorldEffectHandler worldEffectHandler;

  public EventDispatcher(ActorManager actorManager, WorldEffectHandler worldEffectHandler) {
    this.actorManager = actorManager;
    this.worldEffectHandler = worldEffectHandler;
  }

  public void dispatch(ReplayEvent event) {
    switch (event) {
      case EntityEvent e -> actorManager.handleEvent(e);
      case PlayerActionEvent e -> actorManager.handleEvent(e);
      case BlockChangeEvent e -> worldEffectHandler.setBlock(e.blockPos(), e.newType());
      case BlockInteractEvent ignored -> {} // visual-only, no block state change
      case ChatEvent e -> worldEffectHandler.showChat(e.senderUuid(), e.senderName(), e.message());
      case WeatherChangeEvent e -> worldEffectHandler.setWeather(e.raining(), e.thundering());
      case TimeChangeEvent e -> worldEffectHandler.setTime(e.worldTime());
      case InventoryEvent ignored -> {} // TODO: inventory display
    }
  }

}
