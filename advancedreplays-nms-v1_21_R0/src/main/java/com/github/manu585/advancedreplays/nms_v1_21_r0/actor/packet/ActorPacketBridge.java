package com.github.manu585.advancedreplays.nms_v1_21_r0.actor.packet;

import com.github.manu585.advancedreplays.nms_v1_21_r0.actor.handle.NmsActorHandle;
import java.util.Collection;
import net.minecraft.server.level.ServerPlayer;

public interface ActorPacketBridge {

  void spawn(NmsActorHandle actor, Collection<ServerPlayer> viewers);
  void teleport(NmsActorHandle actor, Collection<ServerPlayer> viewers);
  void destroy(NmsActorHandle actor, Collection<ServerPlayer> viewers);

}
