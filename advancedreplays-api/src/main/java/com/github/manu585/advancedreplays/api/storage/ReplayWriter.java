package com.github.manu585.advancedreplays.api.storage;

import com.github.manu585.advancedreplays.api.event.ReplayEvent;
import com.github.manu585.advancedreplays.api.timeline.Keyframe;
import com.github.manu585.advancedreplays.api.timeline.ReplayMetadata;

import java.io.IOException;
import java.util.List;

/** Writes replay event data and metadata to a storage destination. */
public interface ReplayWriter extends AutoCloseable {

  void writeEvents(int tick, List<ReplayEvent> events) throws IOException;

  void writeKeyframe(Keyframe keyframe) throws IOException;

  void finish(ReplayMetadata metadata) throws IOException;

  @Override
  void close() throws IOException;

}
