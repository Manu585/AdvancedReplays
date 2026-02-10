package com.github.manu585.advancedreplays.api.recording;

import java.util.UUID;

/** Controls a single recording session, providing lifecycle and state access. */
public interface RecordingController {

  UUID replayId();

  UUID playerUuid();

  UUID sessionId();

  void start();

  void stop();

  boolean isRecording();

  int currentTick();

}
