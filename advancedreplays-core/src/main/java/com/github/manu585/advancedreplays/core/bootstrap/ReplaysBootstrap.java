package com.github.manu585.advancedreplays.core.bootstrap;

import com.github.manu585.advancedreplays.api.actor.ActorFactory;
import com.github.manu585.advancedreplays.api.bootstrap.Bootstrap;
import com.github.manu585.advancedreplays.api.storage.StorageBackend;
import com.github.manu585.advancedreplays.core.playback.PlaybackSessionManager;
import com.github.manu585.advancedreplays.core.recording.RecordingSessionManager;
import com.github.manu585.advancedreplays.core.storage.FileStorageBackend;

import java.nio.file.Path;
import java.util.logging.Logger;

/** Core bootstrap implementation managing storage, recording, and playback subsystems. */
public class ReplaysBootstrap implements Bootstrap {

  private static final Logger LOGGER = Logger.getLogger(ReplaysBootstrap.class.getName());

  private final Path dataFolder;
  private StorageBackend storageBackend;
  private RecordingSessionManager recordingManager;
  private PlaybackSessionManager playbackManager;

  public ReplaysBootstrap(Path dataFolder) {
    this.dataFolder = dataFolder;
  }

  @Override
  public void onLoad() {
    this.storageBackend = new FileStorageBackend(dataFolder);
  }

  @Override
  public void onEnable() {
    this.recordingManager = new RecordingSessionManager(storageBackend, "1.21", 767);
    LOGGER.info("AdvancedReplays core enabled");
  }

  public void initPlayback(ActorFactory actorFactory) {
    this.playbackManager = new PlaybackSessionManager(storageBackend, actorFactory);
  }

  @Override
  public void onDisable() {
    if (recordingManager != null) recordingManager.stopAll();
    if (playbackManager != null) playbackManager.stopAll();
    LOGGER.info("AdvancedReplays core disabled");
  }

  public StorageBackend storageBackend() {
    return storageBackend;
  }

  public RecordingSessionManager recordingManager() {
    return recordingManager;
  }

  public PlaybackSessionManager playbackManager() {
    return playbackManager;
  }

}
