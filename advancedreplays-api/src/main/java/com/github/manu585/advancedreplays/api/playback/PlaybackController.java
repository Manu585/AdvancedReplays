package com.github.manu585.advancedreplays.api.playback;

/** Controls replay playback, providing play, pause, seek, and speed operations. */
public interface PlaybackController {

  void play();

  void pause();

  void resume();

  void setSpeed(double multiplier);

  void seekTo(int tick);

  void stepForward();

  void stepBackward();

  int currentTick();

  int totalTicks();

  PlaybackState state();

  void addListener(PlaybackListener listener);

  void destroy();

}
