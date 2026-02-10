package com.github.manu585.advancedreplays.nms_v1_21_r0.actor.packet;

import com.github.manu585.advancedreplays.nms_v1_21_r0.actor.handle.NmsActorHandle;
import java.util.Collection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/** Abstraction for sending actor-related NMS packets to viewers. */
public interface ActorPacketBridge {

  void spawn(NmsActorHandle actor, Collection<ServerPlayer> viewers);
  void teleport(NmsActorHandle actor, Collection<ServerPlayer> viewers);
  void destroy(NmsActorHandle actor, Collection<ServerPlayer> viewers);
  void sendEquipment(NmsActorHandle actor, EquipmentSlot slot, ItemStack item, Collection<ServerPlayer> viewers);
  void sendMetadata(NmsActorHandle actor, Collection<ServerPlayer> viewers);
  void sendAnimation(NmsActorHandle actor, int animationId, Collection<ServerPlayer> viewers);
  void sendEntityStatus(NmsActorHandle actor, byte status, Collection<ServerPlayer> viewers);

}
