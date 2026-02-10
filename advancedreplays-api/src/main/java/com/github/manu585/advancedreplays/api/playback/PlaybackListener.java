package com.github.manu585.advancedreplays.api.playback;

/** Listener for playback lifecycle events such as start, pause, and completion. */
public interface PlaybackListener {

  default void onStateChange(PlaybackState oldState, PlaybackState newState) {}

  default void onTick(int tick) {}

  default void onFinish() {}

}
