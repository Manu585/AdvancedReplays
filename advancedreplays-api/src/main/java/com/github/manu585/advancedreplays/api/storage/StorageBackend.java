package com.github.manu585.advancedreplays.api.storage;

import com.github.manu585.advancedreplays.api.timeline.ReplayMetadata;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/** Abstraction for persisting and retrieving replay recordings, organized by player. */
public interface StorageBackend {

  ReplayWriter openWriter(UUID playerUuid, UUID replayId) throws IOException;

  ReplayReader openReader(UUID playerUuid, UUID replayId) throws IOException;

  List<ReplayMetadata> listReplays() throws IOException;

  List<ReplayMetadata> listReplaysForPlayer(UUID playerUuid) throws IOException;

  boolean deleteReplay(UUID playerUuid, UUID replayId) throws IOException;

}
