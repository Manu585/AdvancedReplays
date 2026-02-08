package com.github.manu585.advancedreplays.nms_v1_21_r0.actor.packet;

import java.util.Collection;
import net.minecraft.server.level.ServerPlayer;

@FunctionalInterface
public interface ViewerProvider {

  Collection<ServerPlayer> viewers();

}
