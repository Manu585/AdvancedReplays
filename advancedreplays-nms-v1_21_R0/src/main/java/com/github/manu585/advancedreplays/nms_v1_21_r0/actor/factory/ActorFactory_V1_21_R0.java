package com.github.manu585.advancedreplays.nms_v1_21_r0.actor.factory;

import com.github.manu585.advancedreplays.api.actor.Actor;
import com.github.manu585.advancedreplays.api.actor.ActorFactory;
import com.github.manu585.advancedreplays.api.actor.specs.ActorSpec;
import com.github.manu585.advancedreplays.api.actor.specs.PlayerSpec;
import com.github.manu585.advancedreplays.nms_v1_21_r0.actor.PlayerReplayActor;
import com.github.manu585.advancedreplays.nms_v1_21_r0.actor.world.LevelResolver;
import net.minecraft.server.MinecraftServer;

public class ActorFactory_V1_21_R0 implements ActorFactory {

  private final MinecraftServer minecraftServer;
  private final LevelResolver levelResolver;

  public ActorFactory_V1_21_R0(MinecraftServer minecraftServer, LevelResolver levelResolver) {
    this.minecraftServer = minecraftServer;
    this.levelResolver = levelResolver;
  }


  @Override
  public Actor createInstance(ActorSpec actorSpec) {
    return switch (actorSpec) {
      case PlayerSpec player -> createPlayerActor(player);

      // TODO: 2026.02.08 gogg: add more types.
      default -> throw new IllegalStateException("Unexpected value: " + actorSpec);
    };
  }

  private Actor createPlayerActor(PlayerSpec playerSpec) {
    return new PlayerReplayActor(playerSpec.actorProfile(), minecraftServer, levelResolver);
  }

}
