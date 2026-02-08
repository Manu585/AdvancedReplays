package com.github.manu585.advancedreplays.nms_v1_21_r0.actor.packet;

import com.github.manu585.advancedreplays.nms_v1_21_r0.actor.handle.NmsActorHandle;
import com.github.manu585.advancedreplays.nms_v1_21_r0.actor.handle.PlayerActorHandle;
import java.util.Collection;
import java.util.Set;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PositionMoveRotation;

public class ActorPacketBridge_V1_21_R0 implements ActorPacketBridge {

  @Override
  public void spawn(NmsActorHandle actor, Collection<ServerPlayer> viewers) {
    Entity entity = actor.entity();

    Packet<?> addEntity = buildAddEntityPacket(entity);
    Packet<?> meta = new ClientboundSetEntityDataPacket(entity.getId(), entity.getEntityData().getNonDefaultValues());
    Packet<?> headRot = new ClientboundRotateHeadPacket(entity, toAngle(entity.getYHeadRot()));

    for (ServerPlayer viewer : viewers) {
      if (viewer == null) {
        continue;
      }

      // only if actor is fake player
      sendPlayerSpawnPrePacketsIfNeeded(actor, viewer);

      viewer.connection.send(addEntity);
      if (meta != null) {
        viewer.connection.send(meta);
      }
      viewer.connection.send(headRot);

      // optional: post packets for player (e.g. remove from tab later)
      sendPlayerSpawnPostPacketsIfNeeded(actor, viewer);
    }
  }

  @Override
  public void teleport(NmsActorHandle actor, Collection<ServerPlayer> viewers) {
    Entity entity = actor.entity();

    Packet<?> tp = ClientboundTeleportEntityPacket.teleport(
            entity.getId(),
            absoluteChange(entity),
            Set.of(),
            entity.onGround()
    );

    Packet<?> head = new ClientboundRotateHeadPacket(entity, toAngle(entity.getYHeadRot()));

    for (ServerPlayer viewer : viewers) {
      if (viewer == null) {
        continue;
      }
      viewer.connection.send(tp);
      viewer.connection.send(head);
    }
  }

  @Override
  public void destroy(NmsActorHandle actor, Collection<ServerPlayer> viewers) {
    Packet<?> remove = new ClientboundRemoveEntitiesPacket(actor.entityId());
    for (ServerPlayer viewer : viewers) {
      if (viewer == null) {
        continue;
      }
      viewer.connection.send(remove);
    }
  }

  private void sendPlayerSpawnPrePacketsIfNeeded(NmsActorHandle actor, ServerPlayer viewer) {
    if (!(actor instanceof PlayerActorHandle playerHandle)) return;

    // TODO: PlayerInfoUpdate add packet
  }

  private void sendPlayerSpawnPostPacketsIfNeeded(NmsActorHandle actor, ServerPlayer viewer) {
    if (!(actor instanceof PlayerActorHandle playerHandle)) return;

    // TODO: tab remove packet
  }

  private static PositionMoveRotation absoluteChange(Entity entity) {
    return new PositionMoveRotation(
            entity.position(),
            entity.getDeltaMovement(),
            entity.getYRot(),
            entity.getXRot()
    );
  }

  private static byte toAngle(float degrees) {
    return (byte) (degrees * 256.0F / 360.0F);
  }

  private static Packet<?> buildAddEntityPacket(Entity entity) {
    return new ClientboundAddEntityPacket(
            entity.getId(),
            entity.getUUID(),
            entity.getX(),
            entity.getY(),
            entity.getZ(),
            entity.getXRot(),
            entity.getYRot(),
            entity.getType(),
            0,
            entity.getDeltaMovement(),
            entity.getYHeadRot()
    );
  }

}
