package com.github.manu585.advancedreplays.core.playback;

import com.github.manu585.advancedreplays.api.actor.Actor;
import com.github.manu585.advancedreplays.api.actor.ActorController;
import com.github.manu585.advancedreplays.api.domain.ReplayPosition;
import com.github.manu585.advancedreplays.api.event.*;
import com.github.manu585.advancedreplays.api.timeline.EntitySnapshot;

/** Default implementation of actor control, delegating to an actor factory for spawn and movement. */
public final class DefaultActorController implements ActorController {

  private final Actor actor;
  private final int replayEntityId;

  public DefaultActorController(Actor actor, int replayEntityId) {
    this.actor = actor;
    this.replayEntityId = replayEntityId;
  }

  @Override
  public Actor actor() {
    return actor;
  }

  @Override
  public int replayEntityId() {
    return replayEntityId;
  }

  @Override
  public void applyEvent(ReplayEvent event) {
    switch (event) {
      case EntityMoveEvent e -> actor.teleport(e.position());
      case EntityEquipmentEvent e -> {} // TODO: apply equipment via packet bridge
      case EntityMetadataEvent e -> {} // TODO: apply metadata
      case EntityDamageEvent e -> {} // TODO: play damage animation
      case PlayerSwingEvent e -> {} // TODO: play swing animation
      case PlayerSneakEvent e -> {} // TODO: set sneaking pose
      case PlayerSprintEvent e -> {} // TODO: set sprinting
      default -> {}
    }
  }

  @Override
  public void applySnapshot(EntitySnapshot snapshot) {
    actor.teleport(snapshot.position());
    // TODO: apply equipment and metadata from snapshot
  }

}
