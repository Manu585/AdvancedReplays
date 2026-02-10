package com.github.manu585.advancedreplays.core.recording;

import com.github.manu585.advancedreplays.api.event.ReplayEvent;
import com.github.manu585.advancedreplays.api.recording.EventCollector;
import com.github.manu585.advancedreplays.api.recording.RecordingController;
import com.github.manu585.advancedreplays.api.storage.ReplayWriter;
import com.github.manu585.advancedreplays.api.storage.StorageBackend;
import com.github.manu585.advancedreplays.api.timeline.KeyframeIndex;
import com.github.manu585.advancedreplays.api.timeline.ReplayMetadata;
import com.github.manu585.advancedreplays.core.storage.SplittingReplayWriter;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Manages the lifecycle of a single per-player recording session. */
public final class RecordingSession implements RecordingController {

  private static final Logger LOGGER = Logger.getLogger(RecordingSession.class.getName());

  private final UUID replayId;
  private final UUID playerUuid;
  private final String playerName;
  private final UUID sessionId;
  private final String worldName;
  private final StorageBackend storageBackend;
  private final String serverVersion;
  private final int protocolVersion;
  private final AtomicBoolean recording = new AtomicBoolean(false);
  private final AtomicInteger tickCounter = new AtomicInteger(0);

  private int partIndex;
  private Instant startTime;
  private ReplayWriter writer;
  private AsyncEventCollector collector;

  public RecordingSession(UUID replayId, UUID playerUuid, String playerName, UUID sessionId,
                          int partIndex, String worldName, StorageBackend storageBackend,
                          String serverVersion, int protocolVersion) {
    this.replayId = replayId;
    this.playerUuid = playerUuid;
    this.playerName = playerName;
    this.sessionId = sessionId;
    this.partIndex = partIndex;
    this.worldName = worldName;
    this.storageBackend = storageBackend;
    this.serverVersion = serverVersion;
    this.protocolVersion = protocolVersion;
  }

  @Override
  public UUID replayId() {
    return replayId;
  }

  @Override
  public UUID playerUuid() {
    return playerUuid;
  }

  @Override
  public UUID sessionId() {
    return sessionId;
  }

  @Override
  public void start() {
    if (!recording.compareAndSet(false, true)) return;

    try {
      this.startTime = Instant.now();
      this.writer = new SplittingReplayWriter(
          storageBackend, playerUuid, playerName, sessionId, replayId, partIndex,
          worldName, serverVersion, protocolVersion
      );
      var keyframeGenerator = new KeyframeGenerator(KeyframeIndex.DEFAULT_INTERVAL);
      this.collector = new AsyncEventCollector(writer, keyframeGenerator, 50);
      LOGGER.info("Recording started for player '" + playerName + "' (" + playerUuid + ") with ID " + replayId);
    } catch (IOException e) {
      recording.set(false);
      LOGGER.log(Level.SEVERE, "Failed to start recording", e);
    }
  }

  @Override
  public void stop() {
    if (!recording.compareAndSet(true, false)) return;

    try {
      collector.close();

      ReplayMetadata metadata = new ReplayMetadata(
          replayId, worldName, startTime, tickCounter.get(),
          serverVersion, protocolVersion, "server",
          playerUuid, playerName, sessionId, partIndex
      );
      writer.finish(metadata);
      writer.close();

      LOGGER.info("Recording stopped for player '" + playerName + "' — " + tickCounter.get() + " ticks captured");
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Failed to stop recording", e);
    }
  }

  @Override
  public boolean isRecording() {
    return recording.get();
  }

  @Override
  public int currentTick() {
    return tickCounter.get();
  }

  public void submitEvent(ReplayEvent event) {
    if (!recording.get() || collector == null) return;
    collector.submit(event);
  }

  public int incrementTick() {
    return tickCounter.incrementAndGet();
  }

  public EventCollector collector() {
    return collector;
  }

  public int partIndex() {
    return partIndex;
  }

  public void setPartIndex(int partIndex) {
    this.partIndex = partIndex;
  }

  public ReplayWriter writer() {
    return writer;
  }

  public String worldName() {
    return worldName;
  }

  public String playerName() {
    return playerName;
  }

}
