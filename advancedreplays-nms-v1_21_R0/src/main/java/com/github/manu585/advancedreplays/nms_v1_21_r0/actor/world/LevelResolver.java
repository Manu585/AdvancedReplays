package com.github.manu585.advancedreplays.nms_v1_21_r0.actor.world;

import net.minecraft.server.level.ServerLevel;

@FunctionalInterface
public interface LevelResolver {

  ServerLevel resolve(String worldName);

}
