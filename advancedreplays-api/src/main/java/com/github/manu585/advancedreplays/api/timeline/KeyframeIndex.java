package com.github.manu585.advancedreplays.api.timeline;

import java.util.List;
import java.util.Optional;

/** Index of keyframes within a replay, providing efficient lookup for seeking. */
public record KeyframeIndex(List<Keyframe> keyframes) {

  public static final int DEFAULT_INTERVAL = 200;

  public Optional<Keyframe> nearestBefore(int tick) {
    Keyframe best = null;
    for (Keyframe kf : keyframes) {
      if (kf.tick() <= tick) {
        best = kf;
      } else {
        break;
      }
    }
    return Optional.ofNullable(best);
  }

}
