package com.github.manu585.advancedreplays.core.playback;

import com.github.manu585.advancedreplays.api.actor.ActorFactory;
import com.github.manu585.advancedreplays.api.playback.PlaybackController;
import com.github.manu585.advancedreplays.api.storage.ReplayReader;
import com.github.manu585.advancedreplays.api.storage.StorageBackend;
import com.github.manu585.advancedreplays.api.timeline.ReplayMetadata;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Manages active playback sessions, coordinating reader, dispatcher, and actor lifecycle. */
public final class PlaybackSessionManager {

  private static final Logger LOGGER = Logger.getLogger(PlaybackSessionManager.class.getName());

  private final StorageBackend storageBackend;
  private final ActorFactory actorFactory;
  private final Map<UUID, PlaybackSession> activeSessions = new ConcurrentHashMap<>();
  private WorldEffectHandler worldEffectHandler;

  public PlaybackSessionManager(StorageBackend storageBackend, ActorFactory actorFactory) {
    this.storageBackend = storageBackend;
    this.actorFactory = actorFactory;
  }

  public void setWorldEffectHandler(WorldEffectHandler worldEffectHandler) {
    this.worldEffectHandler = worldEffectHandler;
  }

  public PlaybackSession startPlayback(UUID playerUuid, UUID replayId) throws IOException {
    if (activeSessions.containsKey(replayId)) {
      throw new IllegalStateException("Replay already playing: " + replayId);
    }

    ReplayReader reader = storageBackend.openReader(playerUuid, replayId);
    ReplayMetadata metadata = reader.metadata();
    ActorManager actorManager = new ActorManager(actorFactory);

    WorldEffectHandler handler = worldEffectHandler;
    if (handler == null) {
      handler = new NoopWorldEffectHandler();
    }

    EventDispatcher dispatcher = new EventDispatcher(actorManager, handler);
    PlaybackSession session = new PlaybackSession(reader, dispatcher, actorManager, metadata);
    activeSessions.put(replayId, session);
    session.play();
    return session;
  }

  public Optional<PlaybackSession> getSession(UUID replayId) {
    return Optional.ofNullable(activeSessions.get(replayId));
  }

  public PlaybackSession getAnySession() {
    return activeSessions.values().stream().findFirst().orElse(null);
  }

  public void stopPlayback(UUID replayId) {
    PlaybackSession session = activeSessions.remove(replayId);
    if (session != null) {
      session.destroy();
    }
  }

  public void tickAll() {
    for (PlaybackSession session : activeSessions.values()) {
      session.tick();
    }
  }

  public void stopAll() {
    for (UUID id : new ArrayList<>(activeSessions.keySet())) {
      stopPlayback(id);
    }
  }

  private static final class NoopWorldEffectHandler implements WorldEffectHandler {

    @Override public void setBlock(com.github.manu585.advancedreplays.api.domain.BlockPos pos, String blockType) {}
    @Override public void setWeather(boolean raining, boolean thundering) {}
    @Override public void setTime(long worldTime) {}
    @Override public void showChat(UUID senderUuid, String senderName, String message) {}

  }

}
