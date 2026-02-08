package com.github.manu585.advancedreplays.api.actor;

import com.github.manu585.advancedreplays.api.actor.specs.ActorSpec;
import com.github.manu585.advancedreplays.api.domain.ReplayPosition;

public interface ActorFactory {

  Actor createInstance(ActorSpec actorSpec);

  default Actor spawnActor(ActorSpec spec, ReplayPosition pos) {
    Actor actor = createInstance(spec);
    actor.spawn(pos);
    return actor;
  }

}
