package com.github.manu585.advancedreplays.core.storage;

import com.github.manu585.advancedreplays.api.event.ReplayEvent;
import com.github.manu585.advancedreplays.api.storage.ReplayWriter;
import com.github.manu585.advancedreplays.api.storage.StorageBackend;
import com.github.manu585.advancedreplays.api.timeline.Keyframe;
import com.github.manu585.advancedreplays.api.timeline.ReplayMetadata;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/** Decorator that transparently splits replay output into multiple parts when size exceeds a threshold. */
public final class SplittingReplayWriter implements ReplayWriter {

  private static final long DEFAULT_MAX_BYTES = 10L * 1024 * 1024; // 10 MB

  private final StorageBackend storageBackend;
  private final UUID playerUuid;
  private final String playerName;
  private final UUID sessionId;
  private final String worldName;
  private final String serverVersion;
  private final int protocolVersion;
  private final long maxBytesPerPart;

  private FileReplayWriter currentWriter;
  private int partIndex;
  private UUID currentReplayId;
  private int lastTick;
  private boolean closed = false;

  public SplittingReplayWriter(StorageBackend storageBackend, UUID playerUuid, String playerName,
                                UUID sessionId, UUID initialReplayId, int initialPartIndex,
                                String worldName, String serverVersion, int protocolVersion,
                                long maxBytesPerPart) throws IOException {
    this.storageBackend = storageBackend;
    this.playerUuid = playerUuid;
    this.playerName = playerName;
    this.sessionId = sessionId;
    this.worldName = worldName;
    this.serverVersion = serverVersion;
    this.protocolVersion = protocolVersion;
    this.maxBytesPerPart = maxBytesPerPart;
    this.partIndex = initialPartIndex;
    this.currentReplayId = initialReplayId;
    this.currentWriter = (FileReplayWriter) storageBackend.openWriter(playerUuid, initialReplayId);
  }

  public SplittingReplayWriter(StorageBackend storageBackend, UUID playerUuid, String playerName,
                                UUID sessionId, UUID initialReplayId, int initialPartIndex,
                                String worldName, String serverVersion, int protocolVersion) throws IOException {
    this(storageBackend, playerUuid, playerName, sessionId, initialReplayId, initialPartIndex,
        worldName, serverVersion, protocolVersion, DEFAULT_MAX_BYTES);
  }

  @Override
  public void writeEvents(int tick, List<ReplayEvent> events) throws IOException {
    ensureOpen();
    this.lastTick = tick;

    if (currentWriter.bytesWritten() >= maxBytesPerPart) {
      splitPart();
    }

    currentWriter.writeEvents(tick, events);
  }

  @Override
  public void writeKeyframe(Keyframe keyframe) throws IOException {
    ensureOpen();
    currentWriter.writeKeyframe(keyframe);
  }

  @Override
  public void finish(ReplayMetadata metadata) throws IOException {
    ensureOpen();
    ReplayMetadata partMetadata = new ReplayMetadata(
        currentReplayId, metadata.worldName(), metadata.startTimestamp(),
        metadata.durationTicks(), metadata.serverVersion(), metadata.protocolVersion(),
        metadata.origin(), playerUuid, playerName, sessionId, partIndex
    );
    currentWriter.finish(partMetadata);
  }

  @Override
  public void close() throws IOException {
    if (closed) return;
    closed = true;
    if (currentWriter != null) {
      currentWriter.close();
    }
  }

  public long bytesWritten() {
    return currentWriter != null ? currentWriter.bytesWritten() : 0;
  }

  public int partIndex() {
    return partIndex;
  }

  public UUID currentReplayId() {
    return currentReplayId;
  }

  private void splitPart() throws IOException {
    ReplayMetadata partMeta = new ReplayMetadata(
        currentReplayId, worldName, java.time.Instant.now(), lastTick,
        serverVersion, protocolVersion, "server",
        playerUuid, playerName, sessionId, partIndex
    );
    currentWriter.finish(partMeta);
    currentWriter.close();

    partIndex++;
    currentReplayId = UUID.randomUUID();
    currentWriter = (FileReplayWriter) storageBackend.openWriter(playerUuid, currentReplayId);
  }

  private void ensureOpen() throws IOException {
    if (closed) throw new IOException("Writer is closed");
  }

}
