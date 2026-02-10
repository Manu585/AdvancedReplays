package com.github.manu585.advancedreplays.nms_v1_21_r0.actor.world;

import net.minecraft.server.level.ServerLevel;

/** Functional interface for resolving NMS ServerLevel instances by world name. */
@FunctionalInterface
public interface LevelResolver {

  ServerLevel resolve(String worldName);

}
