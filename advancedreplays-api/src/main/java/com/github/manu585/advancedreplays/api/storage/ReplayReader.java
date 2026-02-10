package com.github.manu585.advancedreplays.api.storage;

import com.github.manu585.advancedreplays.api.event.ReplayEvent;
import com.github.manu585.advancedreplays.api.timeline.Keyframe;
import com.github.manu585.advancedreplays.api.timeline.KeyframeIndex;
import com.github.manu585.advancedreplays.api.timeline.ReplayFrame;
import com.github.manu585.advancedreplays.api.timeline.ReplayMetadata;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

/** Reads replay event data and metadata from a storage source. */
public interface ReplayReader extends AutoCloseable {

  ReplayMetadata metadata() throws IOException;

  KeyframeIndex keyframeIndex() throws IOException;

  List<ReplayEvent> readEvents(int fromTick, int toTick) throws IOException;

  Keyframe readKeyframe(int tick) throws IOException;

  Stream<ReplayFrame> streamAllEvents() throws IOException;

  @Override
  void close() throws IOException;

}
