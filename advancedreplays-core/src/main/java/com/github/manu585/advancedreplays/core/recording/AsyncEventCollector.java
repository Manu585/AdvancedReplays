package com.github.manu585.advancedreplays.core.recording;

import com.github.manu585.advancedreplays.api.event.ReplayEvent;
import com.github.manu585.advancedreplays.api.recording.EventCollector;
import com.github.manu585.advancedreplays.api.storage.ReplayWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Asynchronous event collector that buffers and flushes replay events to a writer off the main thread. */
public final class AsyncEventCollector implements EventCollector {

  private static final Logger LOGGER = Logger.getLogger(AsyncEventCollector.class.getName());

  private final ConcurrentLinkedQueue<ReplayEvent> queue = new ConcurrentLinkedQueue<>();
  private final ReplayWriter writer;
  private final KeyframeGenerator keyframeGenerator;
  private final AtomicBoolean running = new AtomicBoolean(true);
  private final Thread flushThread;

  public AsyncEventCollector(ReplayWriter writer, KeyframeGenerator keyframeGenerator, long flushIntervalMs) {
    this.writer = writer;
    this.keyframeGenerator = keyframeGenerator;

    this.flushThread = Thread.ofVirtual().name("replay-flush").start(() -> {
      while (running.get()) {
        try {
          Thread.sleep(flushIntervalMs);
          flush();
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          break;
        }
      }
      flush();
    });
  }

  @Override
  public void submit(ReplayEvent event) {
    if (!running.get()) return;
    queue.add(event);
  }

  @Override
  public void flush() {
    List<ReplayEvent> batch = new ArrayList<>();
    ReplayEvent event;
    while ((event = queue.poll()) != null) {
      batch.add(event);
    }

    if (batch.isEmpty()) return;

    try {
      int currentTick = batch.getFirst().tick();
      List<ReplayEvent> tickBatch = new ArrayList<>();

      for (ReplayEvent e : batch) {
        if (e.tick() != currentTick) {
          writer.writeEvents(currentTick, tickBatch);
          keyframeGenerator.processEvents(currentTick, tickBatch);
          tickBatch.clear();
          currentTick = e.tick();
        }
        tickBatch.add(e);
      }

      if (!tickBatch.isEmpty()) {
        writer.writeEvents(currentTick, tickBatch);
        keyframeGenerator.processEvents(currentTick, tickBatch);
      }

      var keyframe = keyframeGenerator.generateIfDue();
      if (keyframe != null) {
        writer.writeKeyframe(keyframe);
      }
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Failed to flush replay events", e);
    }
  }

  @Override
  public void close() {
    running.set(false);
    flushThread.interrupt();
    try {
      flushThread.join(5000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

}
