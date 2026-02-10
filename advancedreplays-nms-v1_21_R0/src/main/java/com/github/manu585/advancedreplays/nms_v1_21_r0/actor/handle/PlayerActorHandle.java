package com.github.manu585.advancedreplays.nms_v1_21_r0.actor.handle;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

/** Handle wrapping a fake NMS ServerPlayer for player-type replay actors. */
public record PlayerActorHandle(ServerPlayer player) implements NmsActorHandle {

  @Override
  public int entityId() {
    return player.getId();
  }

  @Override
  public Entity entity() {
    return player;
  }

}
