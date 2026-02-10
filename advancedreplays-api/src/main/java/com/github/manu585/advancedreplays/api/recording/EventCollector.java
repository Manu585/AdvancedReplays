package com.github.manu585.advancedreplays.api.recording;

import com.github.manu585.advancedreplays.api.event.ReplayEvent;

/** Collects and buffers replay events during an active recording session. */
public interface EventCollector {

  void submit(ReplayEvent event);

  void flush();

  void close();

}
