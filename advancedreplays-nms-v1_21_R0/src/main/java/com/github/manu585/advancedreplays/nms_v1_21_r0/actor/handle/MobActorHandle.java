package com.github.manu585.advancedreplays.nms_v1_21_r0.actor.handle;

import net.minecraft.world.entity.Entity;

/** Handle wrapping an NMS mob entity for mob-type replay actors. */
public record MobActorHandle(Entity mob) implements NmsActorHandle {

  @Override
  public int entityId() {
    return mob.getId();
  }

  @Override
  public Entity entity() {
    return mob;
  }

}
