package com.github.manu585.advancedreplays.nms_v1_21_r0.actor.handle;

import net.minecraft.world.entity.Entity;

public sealed interface NmsActorHandle permits PlayerActorHandle, MobActorHandle {

  int entityId();

  Entity entity();

}
