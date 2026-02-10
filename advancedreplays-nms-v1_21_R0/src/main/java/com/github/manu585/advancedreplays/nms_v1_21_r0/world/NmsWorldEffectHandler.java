package com.github.manu585.advancedreplays.nms_v1_21_r0.world;

import com.github.manu585.advancedreplays.api.domain.BlockPos;
import com.github.manu585.advancedreplays.core.playback.WorldEffectHandler;
import com.github.manu585.advancedreplays.nms_v1_21_r0.actor.packet.ViewerProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

/** NMS implementation of world effects, applying block changes, weather, time, and chat via server internals. */
public class NmsWorldEffectHandler implements WorldEffectHandler {

  private final ViewerProvider viewerProvider;

  public NmsWorldEffectHandler(ViewerProvider viewerProvider) {
    this.viewerProvider = viewerProvider;
  }

  @Override
  public void setBlock(BlockPos pos, String blockType) {
    // TODO: look up block by ResourceLocation from BuiltInRegistries, send ClientboundBlockUpdatePacket
  }

  @Override
  public void setWeather(boolean raining, boolean thundering) {
    // TODO: send ClientboundGameEventPacket for weather changes
  }

  @Override
  public void setTime(long worldTime) {
    // TODO: send ClientboundSetTimePacket
  }

  @Override
  public void showChat(UUID senderUuid, String senderName, String message) {
    Component chatMessage = Component.literal("<" + senderName + "> " + message);
    var packet = new ClientboundSystemChatPacket(chatMessage, false);

    for (ServerPlayer viewer : viewerProvider.viewers()) {
      if (viewer == null) continue;
      viewer.connection.send(packet);
    }
  }

}
