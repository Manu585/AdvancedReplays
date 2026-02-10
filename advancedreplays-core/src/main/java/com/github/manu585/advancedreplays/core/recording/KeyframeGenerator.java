package com.github.manu585.advancedreplays.core.recording;

import com.github.manu585.advancedreplays.api.domain.ReplayPosition;
import com.github.manu585.advancedreplays.api.event.*;
import com.github.manu585.advancedreplays.api.timeline.*;

import java.util.*;

/** Generates keyframes at regular tick intervals during recording for seek support. */
public final class KeyframeGenerator {

  private final int interval;
  private int lastKeyframeTick = -1;
  private int latestTick = 0;

  private final Map<Integer, EntitySnapshot> entityStates = new HashMap<>();
  private boolean raining = false;
  private boolean thundering = false;
  private long worldTime = 0;

  public KeyframeGenerator(int interval) {
    this.interval = interval;
  }

  public void processEvents(int tick, List<ReplayEvent> events) {
    latestTick = tick;
    for (ReplayEvent event : events) {
      switch (event) {
        case EntitySpawnEvent e -> entityStates.put(e.entityId(), new EntitySnapshot(
            e.entityId(), e.entityType(), e.position(), e.profile(), Map.of(), Map.of()
        ));
        case EntityDeathEvent e -> entityStates.remove(e.entityId());
        case EntityMoveEvent e -> {
          EntitySnapshot existing = entityStates.get(e.entityId());
          if (existing != null) {
            entityStates.put(e.entityId(), new EntitySnapshot(
                existing.entityId(), existing.entityType(), e.position(),
                existing.profile(), existing.equipmentData(), existing.metadataSnapshot()
            ));
          }
        }
        case WeatherChangeEvent e -> { raining = e.raining(); thundering = e.thundering(); }
        case TimeChangeEvent e -> worldTime = e.worldTime();
        default -> {} // TODO: track equipment and metadata changes
      }
    }
  }

  public Keyframe generateIfDue() {
    if (lastKeyframeTick == -1 && latestTick >= 0) {
      lastKeyframeTick = 0;
      return buildKeyframe(0);
    }
    if (latestTick - lastKeyframeTick >= interval) {
      lastKeyframeTick = latestTick;
      return buildKeyframe(latestTick);
    }
    return null;
  }

  private Keyframe buildKeyframe(int tick) {
    return new Keyframe(
        tick,
        List.copyOf(entityStates.values()),
        Map.of(),
        new WorldStateSnapshot(raining, thundering, worldTime),
        0
    );
  }

}
