package com.github.manu585.advancedreplays.core.recording;

import com.github.manu585.advancedreplays.api.storage.StorageBackend;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Manages active per-player recording sessions, handling start, stop, and tick operations. */
public final class RecordingSessionManager {

  private final Map<UUID, RecordingSession> activeByPlayer = new ConcurrentHashMap<>();
  private final StorageBackend storageBackend;
  private final String serverVersion;
  private final int protocolVersion;

  public RecordingSessionManager(StorageBackend storageBackend, String serverVersion, int protocolVersion) {
    this.storageBackend = storageBackend;
    this.serverVersion = serverVersion;
    this.protocolVersion = protocolVersion;
  }

  public RecordingSession startRecording(UUID playerUuid, String playerName, String worldName) {
    if (activeByPlayer.containsKey(playerUuid)) {
      throw new IllegalStateException("Already recording for player: " + playerUuid);
    }

    UUID replayId = UUID.randomUUID();
    UUID sessionId = UUID.randomUUID();
    RecordingSession session = new RecordingSession(
            replayId, playerUuid, playerName, sessionId, 0,
            worldName, storageBackend, serverVersion, protocolVersion
    );
    activeByPlayer.put(playerUuid, session);
    session.start();
    return session;
  }

  public RecordingSession stopRecording(UUID playerUuid) {
    RecordingSession session = activeByPlayer.remove(playerUuid);
    if (session != null) {
      session.stop();
    }
    return session;
  }

  public Optional<RecordingSession> getSession(UUID playerUuid) {
    return Optional.ofNullable(activeByPlayer.get(playerUuid));
  }

  public boolean isRecording(UUID playerUuid) {
    RecordingSession session = activeByPlayer.get(playerUuid);
    return session != null && session.isRecording();
  }

  public Collection<RecordingSession> allSessions() {
    return activeByPlayer.values();
  }

  public void stopAll() {
    for (UUID player : activeByPlayer.keySet()) {
      stopRecording(player);
    }
  }

  public void tickAll() {
    for (RecordingSession session : activeByPlayer.values()) {
      if (session.isRecording()) {
        session.incrementTick();
      }
    }
  }

}
