package com.github.manu585.advancedreplays.nms_v1_21_r0.actor.factory;

import com.github.manu585.advancedreplays.api.actor.Actor;
import com.github.manu585.advancedreplays.api.actor.ActorFactory;
import com.github.manu585.advancedreplays.api.actor.specs.ActorSpec;
import com.github.manu585.advancedreplays.api.actor.specs.MobSpec;
import com.github.manu585.advancedreplays.api.actor.specs.PlayerSpec;
import com.github.manu585.advancedreplays.nms_v1_21_r0.actor.PlayerReplayActor;
import com.github.manu585.advancedreplays.nms_v1_21_r0.actor.packet.ActorPacketBridge;
import com.github.manu585.advancedreplays.nms_v1_21_r0.actor.packet.ViewerProvider;
import com.github.manu585.advancedreplays.nms_v1_21_r0.actor.world.LevelResolver;
import net.minecraft.server.MinecraftServer;

/** Version-specific actor factory for 1.21, pattern-matching on ActorSpec to create actors. */
public class ActorFactory_V1_21_R0 implements ActorFactory {

  private final MinecraftServer minecraftServer;
  private final LevelResolver levelResolver;
  private final ActorPacketBridge packetBridge;
  private final ViewerProvider viewerProvider;

  public ActorFactory_V1_21_R0(MinecraftServer minecraftServer, LevelResolver levelResolver,
                                ActorPacketBridge packetBridge, ViewerProvider viewerProvider) {
    this.minecraftServer = minecraftServer;
    this.levelResolver = levelResolver;
    this.packetBridge = packetBridge;
    this.viewerProvider = viewerProvider;
  }

  @Override
  public Actor createInstance(ActorSpec actorSpec) {
    return switch (actorSpec) {
      case PlayerSpec player -> createPlayerActor(player);
      case MobSpec mob -> createMobActor(mob);
    };
  }

  private Actor createPlayerActor(PlayerSpec playerSpec) {
    return new PlayerReplayActor(playerSpec.actorProfile(), minecraftServer, levelResolver, packetBridge, viewerProvider);
  }

  private Actor createMobActor(MobSpec mobSpec) {
    // TODO: implement mob actor creation using NMS entity types
    throw new UnsupportedOperationException("Mob actors not yet implemented for type: " + mobSpec.actorType());
  }

}
