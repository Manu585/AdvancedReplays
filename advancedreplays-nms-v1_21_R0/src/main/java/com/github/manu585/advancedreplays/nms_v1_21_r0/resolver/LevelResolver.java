package com.github.manu585.advancedreplays.nms_v1_21_r0.resolver;

import net.minecraft.server.level.ServerLevel;

@FunctionalInterface
public interface LevelResolver {

  ServerLevel resolve(String worldName);

}
