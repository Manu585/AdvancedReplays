package com.github.manu585.advancedreplays.core.storage;

import com.github.manu585.advancedreplays.api.event.ReplayEvent;
import com.github.manu585.advancedreplays.api.event.codec.ReplayEventCodec;
import com.github.manu585.advancedreplays.api.storage.ReplayReader;
import com.github.manu585.advancedreplays.api.timeline.*;

import java.io.*;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.GZIPInputStream;

/** Reads replay events and metadata from GZIP-compressed binary files on disk. */
public final class FileReplayReader implements ReplayReader {

  private static final byte[] MAGIC = {'A', 'R', 'P', 'L'};

  private final Path replayDir;
  private final ReplayEventCodec codec;
  private ReplayMetadata cachedMetadata;
  private KeyframeIndex cachedKeyframeIndex;
  private boolean closed = false;

  public FileReplayReader(Path replayDir, ReplayEventCodec codec) {
    this.replayDir = replayDir;
    this.codec = codec;
  }

  @Override
  public ReplayMetadata metadata() throws IOException {
    if (cachedMetadata != null) return cachedMetadata;
    cachedMetadata = readMetadataFile();
    return cachedMetadata;
  }

  @Override
  public KeyframeIndex keyframeIndex() throws IOException {
    if (cachedKeyframeIndex != null) return cachedKeyframeIndex;
    readMetadataFile();
    return cachedKeyframeIndex;
  }

  @Override
  public List<ReplayEvent> readEvents(int fromTick, int toTick) throws IOException {
    ensureOpen();
    List<ReplayEvent> result = new ArrayList<>();
    try (var stream = openEventStream()) {
      while (true) {
        try {
          int count = stream.readUnsignedShort();
          for (int i = 0; i < count; i++) {
            ReplayEvent event = codec.decode(stream);
            if (event.tick() >= fromTick && event.tick() <= toTick) {
              result.add(event);
            }
            if (event.tick() > toTick) {
              return result;
            }
          }
        } catch (EOFException e) {
          break;
        }
      }
    }
    return result;
  }

  @Override
  public Keyframe readKeyframe(int tick) throws IOException {
    KeyframeIndex index = keyframeIndex();
    return index.nearestBefore(tick).orElse(null);
  }

  @Override
  public Stream<ReplayFrame> streamAllEvents() throws IOException {
    ensureOpen();
    DataInputStream stream = openEventStream();

    Iterator<ReplayFrame> iterator = new Iterator<>() {

      private ReplayFrame next = readNext();

      private ReplayFrame readNext() {
        try {
          int count = stream.readUnsignedShort();
          List<ReplayEvent> events = new ArrayList<>(count);
          int frameTick = -1;
          for (int i = 0; i < count; i++) {
            ReplayEvent event = codec.decode(stream);
            events.add(event);
            if (frameTick == -1) frameTick = event.tick();
          }
          return events.isEmpty() ? null : new ReplayFrame(frameTick, events);
        } catch (EOFException e) {
          closeQuietly(stream);
          return null;
        } catch (IOException e) {
          closeQuietly(stream);
          throw new UncheckedIOException(e);
        }
      }

      @Override
      public boolean hasNext() {
        return next != null;
      }

      @Override
      public ReplayFrame next() {
        if (next == null) throw new NoSuchElementException();
        ReplayFrame current = next;
        next = readNext();
        return current;
      }

    };

    Spliterator<ReplayFrame> spliterator = Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED);
    return StreamSupport.stream(spliterator, false).onClose(() -> closeQuietly(stream));
  }

  @Override
  public void close() throws IOException {
    closed = true;
  }

  private DataInputStream openEventStream() throws IOException {
    Path eventsFile = replayDir.resolve("events.bin");
    var fis = new FileInputStream(eventsFile.toFile());
    var gzip = new GZIPInputStream(fis, 8192);
    var dis = new DataInputStream(new BufferedInputStream(gzip, 8192));

    byte[] magic = new byte[4];
    dis.readFully(magic);
    if (!Arrays.equals(magic, MAGIC)) {
      dis.close();
      throw new IOException("Invalid replay file: bad magic");
    }
    int version = dis.readInt();
    if (version < 1 || version > 2) {
      dis.close();
      throw new IOException("Unsupported replay format version: " + version);
    }
    return dis;
  }

  private ReplayMetadata readMetadataFile() throws IOException {
    Path metadataFile = replayDir.resolve("metadata.bin");
    try (var dis = new DataInputStream(new BufferedInputStream(new FileInputStream(metadataFile.toFile())))) {
      byte[] magic = new byte[4];
      dis.readFully(magic);
      if (!Arrays.equals(magic, MAGIC)) throw new IOException("Invalid metadata file: bad magic");
      int version = dis.readInt();

      UUID replayId = new UUID(dis.readLong(), dis.readLong());
      String worldName = dis.readUTF();
      Instant startTimestamp = Instant.ofEpochMilli(dis.readLong());
      int durationTicks = dis.readInt();
      String serverVersion = dis.readUTF();
      int protocolVersion = dis.readInt();
      String origin = dis.readUTF();

      UUID playerUuid = null;
      String playerName = null;
      UUID sessionId = null;
      int partIndex = 0;

      if (version >= 2) {
        long puMsb = dis.readLong();
        long puLsb = dis.readLong();
        playerUuid = (puMsb == 0 && puLsb == 0) ? null : new UUID(puMsb, puLsb);
        String pn = dis.readUTF();
        playerName = pn.isEmpty() ? null : pn;
        long siMsb = dis.readLong();
        long siLsb = dis.readLong();
        sessionId = (siMsb == 0 && siLsb == 0) ? null : new UUID(siMsb, siLsb);
        partIndex = dis.readInt();
      }

      cachedMetadata = new ReplayMetadata(
          replayId, worldName, startTimestamp, durationTicks,
          serverVersion, protocolVersion,
          origin.isEmpty() ? null : origin,
          playerUuid, playerName, sessionId, partIndex
      );

      int keyframeCount = dis.readInt();
      List<Keyframe> keyframes = new ArrayList<>(keyframeCount);
      for (int i = 0; i < keyframeCount; i++) {
        int tick = dis.readInt();
        long byteOffset = dis.readLong();
        int entityCount = dis.readInt();
        WorldStateSnapshot worldState = null;
        if (dis.readBoolean()) {
          worldState = new WorldStateSnapshot(dis.readBoolean(), dis.readBoolean(), dis.readLong());
        }
        keyframes.add(new Keyframe(tick, List.of(), Map.of(), worldState, byteOffset));
      }
      cachedKeyframeIndex = new KeyframeIndex(keyframes);

      return cachedMetadata;
    }
  }

  private void ensureOpen() throws IOException {
    if (closed) throw new IOException("Reader is closed");
  }

  private static void closeQuietly(Closeable closeable) {
    try { closeable.close(); } catch (IOException ignored) {}
  }

}
