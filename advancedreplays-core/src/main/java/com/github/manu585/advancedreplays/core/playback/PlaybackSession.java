package com.github.manu585.advancedreplays.core.playback;

import com.github.manu585.advancedreplays.api.event.ReplayEvent;
import com.github.manu585.advancedreplays.api.playback.PlaybackController;
import com.github.manu585.advancedreplays.api.playback.PlaybackListener;
import com.github.manu585.advancedreplays.api.playback.PlaybackState;
import com.github.manu585.advancedreplays.api.storage.ReplayReader;
import com.github.manu585.advancedreplays.api.timeline.*;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Drives the tick-by-tick playback of a recorded replay, managing state and dispatching events. */
public final class PlaybackSession implements PlaybackController {

  private static final Logger LOGGER = Logger.getLogger(PlaybackSession.class.getName());
  private static final int BUFFER_WINDOW = 200;

  private final ReplayReader reader;
  private final EventDispatcher dispatcher;
  private final ActorManager actorManager;
  private final ReplayMetadata metadata;
  private final List<PlaybackListener> listeners = new ArrayList<>();

  private volatile PlaybackState state = PlaybackState.IDLE;
  private int currentTick = 0;
  private double speed = 1.0;
  private double tickAccumulator = 0.0;

  private final TreeMap<Integer, List<ReplayEvent>> eventBuffer = new TreeMap<>();
  private int bufferLowTick = 0;
  private int bufferHighTick = -1;

  public PlaybackSession(ReplayReader reader, EventDispatcher dispatcher,
                         ActorManager actorManager, ReplayMetadata metadata) {
    this.reader = reader;
    this.dispatcher = dispatcher;
    this.actorManager = actorManager;
    this.metadata = metadata;
  }

  @Override
  public void play() {
    if (state != PlaybackState.IDLE && state != PlaybackState.FINISHED) return;
    currentTick = 0;
    tickAccumulator = 0.0;
    actorManager.destroyAll();
    bufferEvents(0);
    setState(PlaybackState.PLAYING);
  }

  @Override
  public void pause() {
    if (state != PlaybackState.PLAYING) return;
    setState(PlaybackState.PAUSED);
  }

  @Override
  public void resume() {
    if (state != PlaybackState.PAUSED) return;
    setState(PlaybackState.PLAYING);
  }

  @Override
  public void setSpeed(double multiplier) {
    this.speed = Math.max(0.1, Math.min(multiplier, 10.0));
  }

  @Override
  public void seekTo(int tick) {
    if (state == PlaybackState.IDLE) return;
    PlaybackState previousState = state;
    setState(PlaybackState.SEEKING);

    actorManager.destroyAll();
    eventBuffer.clear();

    try {
      KeyframeIndex index = reader.keyframeIndex();
      Optional<Keyframe> keyframe = index.nearestBefore(tick);

      int startTick = 0;
      if (keyframe.isPresent()) {
        Keyframe kf = keyframe.get();
        startTick = kf.tick();
        for (EntitySnapshot snapshot : kf.entityStates()) {
          actorManager.applySnapshot(snapshot);
        }
      }

      if (startTick < tick) {
        List<ReplayEvent> events = reader.readEvents(startTick, tick);
        for (ReplayEvent event : events) {
          dispatcher.dispatch(event);
        }
      }

      currentTick = tick;
      bufferEvents(tick);
      setState(previousState == PlaybackState.PLAYING ? PlaybackState.PLAYING : PlaybackState.PAUSED);
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Failed to seek", e);
      setState(PlaybackState.PAUSED);
    }
  }

  @Override
  public void stepForward() {
    if (state != PlaybackState.PAUSED) return;
    advanceTick();
  }

  @Override
  public void stepBackward() {
    if (state != PlaybackState.PAUSED) return;
    if (currentTick <= 0) return;
    seekTo(currentTick - 1);
  }

  @Override
  public int currentTick() {
    return currentTick;
  }

  @Override
  public int totalTicks() {
    return metadata.durationTicks();
  }

  @Override
  public PlaybackState state() {
    return state;
  }

  @Override
  public void addListener(PlaybackListener listener) {
    listeners.add(listener);
  }

  @Override
  public void destroy() {
    actorManager.destroyAll();
    eventBuffer.clear();
    setState(PlaybackState.IDLE);
    try {
      reader.close();
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Failed to close reader", e);
    }
  }

  public void tick() {
    if (state != PlaybackState.PLAYING) return;

    tickAccumulator += speed;
    while (tickAccumulator >= 1.0) {
      tickAccumulator -= 1.0;
      advanceTick();
      if (state != PlaybackState.PLAYING) break;
    }
  }

  private void advanceTick() {
    List<ReplayEvent> events = eventBuffer.remove(currentTick);
    if (events != null) {
      for (ReplayEvent event : events) {
        dispatcher.dispatch(event);
      }
    }

    currentTick++;
    for (PlaybackListener listener : listeners) {
      listener.onTick(currentTick);
    }

    if (currentTick >= metadata.durationTicks()) {
      setState(PlaybackState.FINISHED);
      for (PlaybackListener listener : listeners) {
        listener.onFinish();
      }
      return;
    }

    if (currentTick > bufferHighTick - BUFFER_WINDOW / 2) {
      bufferEvents(bufferHighTick + 1);
    }
  }

  private void bufferEvents(int fromTick) {
    int toTick = fromTick + BUFFER_WINDOW;
    try {
      List<ReplayEvent> events = reader.readEvents(fromTick, toTick);
      for (ReplayEvent event : events) {
        eventBuffer.computeIfAbsent(event.tick(), k -> new ArrayList<>()).add(event);
      }
      bufferLowTick = fromTick;
      bufferHighTick = toTick;
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Failed to buffer events", e);
    }
  }

  private void setState(PlaybackState newState) {
    PlaybackState old = this.state;
    this.state = newState;
    for (PlaybackListener listener : listeners) {
      listener.onStateChange(old, newState);
    }
  }

}
