package com.github.manu585.advancedreplays.core.storage;

import com.github.manu585.advancedreplays.api.event.codec.ReplayEventCodec;
import com.github.manu585.advancedreplays.api.storage.ReplayReader;
import com.github.manu585.advancedreplays.api.storage.ReplayWriter;
import com.github.manu585.advancedreplays.api.storage.StorageBackend;
import com.github.manu585.advancedreplays.api.timeline.ReplayMetadata;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** File-system storage backend organizing replays in a player-keyed directory hierarchy. */
public final class FileStorageBackend implements StorageBackend {

  private final Path replaysDir;
  private final ReplayEventCodec codec;

  public FileStorageBackend(Path dataFolder) {
    this.replaysDir = dataFolder.resolve("replays");
    this.codec = new BinaryReplayEventCodec();
  }

  @Override
  public ReplayWriter openWriter(UUID playerUuid, UUID replayId) throws IOException {
    Path replayDir = replaysDir.resolve(playerUuid.toString()).resolve(replayId.toString());
    return new FileReplayWriter(replayDir, codec);
  }

  @Override
  public ReplayReader openReader(UUID playerUuid, UUID replayId) throws IOException {
    Path replayDir = replaysDir.resolve(playerUuid.toString()).resolve(replayId.toString());
    if (!Files.isDirectory(replayDir)) {
      throw new IOException("Replay not found: " + replayId);
    }
    return new FileReplayReader(replayDir, codec);
  }

  @Override
  public List<ReplayMetadata> listReplays() throws IOException {
    List<ReplayMetadata> result = new ArrayList<>();
    if (!Files.isDirectory(replaysDir)) return result;

    try (DirectoryStream<Path> playerDirs = Files.newDirectoryStream(replaysDir)) {
      for (Path playerDir : playerDirs) {
        if (!Files.isDirectory(playerDir)) continue;
        collectReplaysFromPlayerDir(playerDir, result);
      }
    }
    return result;
  }

  @Override
  public List<ReplayMetadata> listReplaysForPlayer(UUID playerUuid) throws IOException {
    List<ReplayMetadata> result = new ArrayList<>();
    Path playerDir = replaysDir.resolve(playerUuid.toString());
    if (!Files.isDirectory(playerDir)) return result;
    collectReplaysFromPlayerDir(playerDir, result);
    return result;
  }

  @Override
  public boolean deleteReplay(UUID playerUuid, UUID replayId) throws IOException {
    Path replayDir = replaysDir.resolve(playerUuid.toString()).resolve(replayId.toString());
    if (!Files.isDirectory(replayDir)) return false;

    try (DirectoryStream<Path> files = Files.newDirectoryStream(replayDir)) {
      for (Path file : files) {
        Files.deleteIfExists(file);
      }
    }
    Files.deleteIfExists(replayDir);
    return true;
  }

  private void collectReplaysFromPlayerDir(Path playerDir, List<ReplayMetadata> result) throws IOException {
    UUID playerUuid;
    try {
      playerUuid = UUID.fromString(playerDir.getFileName().toString());
    } catch (IllegalArgumentException e) {
      return;
    }

    try (DirectoryStream<Path> replayDirs = Files.newDirectoryStream(playerDir)) {
      for (Path replayDir : replayDirs) {
        if (!Files.isDirectory(replayDir)) continue;
        Path metadataFile = replayDir.resolve("metadata.bin");
        if (!Files.exists(metadataFile)) continue;

        try {
          UUID replayId = UUID.fromString(replayDir.getFileName().toString());
          try (ReplayReader reader = openReader(playerUuid, replayId)) {
            result.add(reader.metadata());
          }
        } catch (IllegalArgumentException | IOException ignored) {
          // skip invalid directories
        }
      }
    }
  }

}
