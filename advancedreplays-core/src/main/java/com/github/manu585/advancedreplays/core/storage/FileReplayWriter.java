package com.github.manu585.advancedreplays.core.storage;

import com.github.manu585.advancedreplays.api.event.ReplayEvent;
import com.github.manu585.advancedreplays.api.event.codec.ReplayEventCodec;
import com.github.manu585.advancedreplays.api.storage.ReplayWriter;
import com.github.manu585.advancedreplays.api.timeline.Keyframe;
import com.github.manu585.advancedreplays.api.timeline.ReplayMetadata;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

/** Writes replay events and metadata to GZIP-compressed binary files on disk. */
public final class FileReplayWriter implements ReplayWriter {

  private static final byte[] MAGIC = {'A', 'R', 'P', 'L'};
  private static final int FORMAT_VERSION = 2;

  private final Path eventsFile;
  private final Path metadataFile;
  private final ReplayEventCodec codec;
  private final DataOutputStream eventStream;
  private final List<Keyframe> keyframes = new ArrayList<>();
  private long bytesWritten = 0;
  private boolean closed = false;

  public FileReplayWriter(Path replayDir, ReplayEventCodec codec) throws IOException {
    this.codec = codec;
    Files.createDirectories(replayDir);
    this.eventsFile = replayDir.resolve("events.bin");
    this.metadataFile = replayDir.resolve("metadata.bin");

    var fos = new FileOutputStream(eventsFile.toFile());
    var gzip = new GZIPOutputStream(fos, 8192);
    this.eventStream = new DataOutputStream(new BufferedOutputStream(gzip, 8192));

    eventStream.write(MAGIC);
    eventStream.writeInt(FORMAT_VERSION);
    bytesWritten += MAGIC.length + 4;
  }

  @Override
  public void writeEvents(int tick, List<ReplayEvent> events) throws IOException {
    ensureOpen();
    eventStream.writeShort(events.size());
    bytesWritten += 2;
    for (ReplayEvent event : events) {
      codec.encode(event, eventStream);
    }
  }

  @Override
  public void writeKeyframe(Keyframe keyframe) throws IOException {
    ensureOpen();
    keyframes.add(new Keyframe(
        keyframe.tick(),
        keyframe.entityStates(),
        keyframe.blockOverrides(),
        keyframe.worldState(),
        bytesWritten
    ));
  }

  @Override
  public void finish(ReplayMetadata metadata) throws IOException {
    ensureOpen();
    writeMetadataFile(metadata);
  }

  @Override
  public void close() throws IOException {
    if (closed) return;
    closed = true;
    eventStream.flush();
    eventStream.close();
  }

  public long bytesWritten() {
    return bytesWritten;
  }

  private void writeMetadataFile(ReplayMetadata metadata) throws IOException {
    try (var dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(metadataFile.toFile())))) {
      dos.write(MAGIC);
      dos.writeInt(FORMAT_VERSION);

      dos.writeLong(metadata.replayId().getMostSignificantBits());
      dos.writeLong(metadata.replayId().getLeastSignificantBits());
      dos.writeUTF(metadata.worldName());
      dos.writeLong(metadata.startTimestamp().toEpochMilli());
      dos.writeInt(metadata.durationTicks());
      dos.writeUTF(metadata.serverVersion());
      dos.writeInt(metadata.protocolVersion());
      dos.writeUTF(metadata.origin() != null ? metadata.origin() : "");

      // v2 fields
      dos.writeLong(metadata.playerUuid() != null ? metadata.playerUuid().getMostSignificantBits() : 0);
      dos.writeLong(metadata.playerUuid() != null ? metadata.playerUuid().getLeastSignificantBits() : 0);
      dos.writeUTF(metadata.playerName() != null ? metadata.playerName() : "");
      dos.writeLong(metadata.sessionId() != null ? metadata.sessionId().getMostSignificantBits() : 0);
      dos.writeLong(metadata.sessionId() != null ? metadata.sessionId().getLeastSignificantBits() : 0);
      dos.writeInt(metadata.partIndex());

      dos.writeInt(keyframes.size());
      for (Keyframe kf : keyframes) {
        dos.writeInt(kf.tick());
        dos.writeLong(kf.byteOffset());
        dos.writeInt(kf.entityStates().size());
        dos.writeBoolean(kf.worldState() != null);
        if (kf.worldState() != null) {
          dos.writeBoolean(kf.worldState().raining());
          dos.writeBoolean(kf.worldState().thundering());
          dos.writeLong(kf.worldState().worldTime());
        }
      }
    }
  }

  private void ensureOpen() throws IOException {
    if (closed) throw new IOException("Writer is closed");
  }

}
