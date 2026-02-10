package com.github.manu585.advancedreplays.nms_v1_21_r0;

import com.github.manu585.advancedreplays.api.actor.ActorFactory;
import com.github.manu585.advancedreplays.core.NmsProvider;
import com.github.manu585.advancedreplays.core.playback.WorldEffectHandler;
import com.github.manu585.advancedreplays.nms_v1_21_r0.actor.factory.ActorFactory_V1_21_R0;
import com.github.manu585.advancedreplays.nms_v1_21_r0.actor.packet.ActorPacketBridge_V1_21_R0;
import com.github.manu585.advancedreplays.nms_v1_21_r0.actor.packet.ViewerProvider;
import com.github.manu585.advancedreplays.nms_v1_21_r0.world.NmsWorldEffectHandler;
import net.minecraft.server.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;

import java.util.stream.Collectors;

/**
 * NMS provider implementation for Minecraft 1.21, supplying version-specific factories and handlers.
 */
public final class NmsBootstrap_V1_21_R0 implements NmsProvider {

  @Override
  public ActorFactory createActorFactory() {
    MinecraftServer minecraftServer = ((CraftServer) Bukkit.getServer()).getServer();
    var packetBridge = new ActorPacketBridge_V1_21_R0();
    ViewerProvider viewerProvider = createViewerProvider();

    return new ActorFactory_V1_21_R0(
        minecraftServer,
        worldName -> {
          var world = Bukkit.getWorld(worldName);
          if (world == null) throw new IllegalArgumentException("World not found: " + worldName);
          return ((CraftWorld) world).getHandle();
        },
        packetBridge,
        viewerProvider
    );
  }

  @Override
  public WorldEffectHandler createWorldEffectHandler() {
    return new NmsWorldEffectHandler(createViewerProvider());
  }

  private static ViewerProvider createViewerProvider() {
    return () -> Bukkit.getOnlinePlayers().stream()
        .map(p -> ((CraftPlayer) p).getHandle())
        .collect(Collectors.toList());
  }

}
