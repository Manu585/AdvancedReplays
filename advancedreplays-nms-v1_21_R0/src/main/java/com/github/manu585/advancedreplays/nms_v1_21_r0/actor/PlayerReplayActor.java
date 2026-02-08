package com.github.manu585.advancedreplays.nms_v1_21_r0.actor;

import com.github.manu585.advancedreplays.api.actor.Actor;
import com.github.manu585.advancedreplays.api.domain.ActorProfile;
import com.github.manu585.advancedreplays.api.domain.ReplayPosition;
import com.github.manu585.advancedreplays.nms_v1_21_r0.actor.util.GameProfiles;
import com.github.manu585.advancedreplays.nms_v1_21_r0.actor.world.LevelResolver;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class PlayerReplayActor implements Actor {

  private final ActorProfile actorProfile;
  private final MinecraftServer server;
  private final LevelResolver levelResolver;

  private ServerPlayer handle; // may be null
  private boolean spawned;

  public PlayerReplayActor(ActorProfile actorProfile, MinecraftServer server, LevelResolver levelResolver) {
    this.actorProfile = actorProfile;
    this.server = server;
    this.levelResolver = levelResolver;
  }

  @Override
  public void spawn(ReplayPosition position) {
    if (spawned) {
      return;
    }

    ServerLevel serverLevel = levelResolver.resolve(position.worldName());
    GameProfile gameProfile = GameProfiles.from(actorProfile);

    this.handle = new ServerPlayer(server, serverLevel, gameProfile, ClientInformation.createDefault());
    handle.moveOrInterpolateTo(new Vec3(position.x(), position.y(), position.z()));

    spawned = true;
  }

  @Override
  public void teleport(ReplayPosition position) {

  }

  @Override
  public void tick() {

  }

  @Override
  public void destroy() {

  }

}
