package com.github.manu585.advancedreplays.core.playback;

import com.github.manu585.advancedreplays.api.domain.BlockPos;

import java.util.UUID;

/** Applies world-level effects (blocks, weather, time, chat) during replay playback. */
public interface WorldEffectHandler {

  void setBlock(BlockPos pos, String blockType);

  void setWeather(boolean raining, boolean thundering);

  void setTime(long worldTime);

  void showChat(UUID senderUuid, String senderName, String message);

}
