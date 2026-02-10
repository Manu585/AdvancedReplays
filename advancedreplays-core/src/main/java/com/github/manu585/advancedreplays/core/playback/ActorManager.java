package com.github.manu585.advancedreplays.core.playback;

import com.github.manu585.advancedreplays.api.actor.Actor;
import com.github.manu585.advancedreplays.api.actor.ActorController;
import com.github.manu585.advancedreplays.api.actor.ActorFactory;
import com.github.manu585.advancedreplays.api.actor.ActorType;
import com.github.manu585.advancedreplays.api.actor.specs.ActorSpec;
import com.github.manu585.advancedreplays.api.actor.specs.MobSpec;
import com.github.manu585.advancedreplays.api.actor.specs.PlayerSpec;
import com.github.manu585.advancedreplays.api.domain.ActorProfile;
import com.github.manu585.advancedreplays.api.event.*;
import com.github.manu585.advancedreplays.api.timeline.EntitySnapshot;

import java.util.HashMap;
import java.util.Map;

/** Manages the set of active replay actors during a playback session. */
public final class ActorManager {

  private final ActorFactory actorFactory;
  private final Map<Integer, ActorController> actors = new HashMap<>();

  public ActorManager(ActorFactory actorFactory) {
    this.actorFactory = actorFactory;
  }

  public void handleEvent(ReplayEvent event) {
    switch (event) {
      case EntitySpawnEvent e -> spawnActor(e);
      case EntityDeathEvent e -> destroyActor(e.entityId());
      case EntityMoveEvent e -> routeToActor(e.entityId(), e);
      case EntityEquipmentEvent e -> routeToActor(e.entityId(), e);
      case EntityDamageEvent e -> routeToActor(e.entityId(), e);
      case EntityMetadataEvent e -> routeToActor(e.entityId(), e);
      case PlayerSwingEvent e -> routeToActor(e.entityId(), e);
      case PlayerSneakEvent e -> routeToActor(e.entityId(), e);
      case PlayerSprintEvent e -> routeToActor(e.entityId(), e);
      default -> {}
    }
  }

  public void applySnapshot(EntitySnapshot snapshot) {
    ActorController controller = actors.get(snapshot.entityId());
    if (controller != null) {
      controller.applySnapshot(snapshot);
    } else {
      ActorSpec spec = specFromSnapshot(snapshot);
      Actor actor = actorFactory.spawnActor(spec, snapshot.position());
      controller = new DefaultActorController(actor, snapshot.entityId());
      controller.applySnapshot(snapshot);
      actors.put(snapshot.entityId(), controller);
    }
  }

  public void destroyAll() {
    for (ActorController controller : actors.values()) {
      controller.actor().destroy();
    }
    actors.clear();
  }

  private void spawnActor(EntitySpawnEvent event) {
    ActorSpec spec = specFromEvent(event);
    Actor actor = actorFactory.spawnActor(spec, event.position());
    ActorController controller = new DefaultActorController(actor, event.entityId());
    actors.put(event.entityId(), controller);
  }

  private void destroyActor(int entityId) {
    ActorController controller = actors.remove(entityId);
    if (controller != null) {
      controller.actor().destroy();
    }
  }

  private void routeToActor(int entityId, ReplayEvent event) {
    ActorController controller = actors.get(entityId);
    if (controller != null) {
      controller.applyEvent(event);
    }
  }

  private ActorSpec specFromEvent(EntitySpawnEvent event) {
    if ("PLAYER".equalsIgnoreCase(event.entityType())) {
      ActorProfile profile = event.profile() != null ? event.profile() : new ActorProfile("ReplayActor", null, null);
      return new PlayerSpec(profile);
    }
    return new MobSpec(parseActorType(event.entityType()));
  }

  private ActorSpec specFromSnapshot(EntitySnapshot snapshot) {
    if ("PLAYER".equalsIgnoreCase(snapshot.entityType())) {
      ActorProfile profile = snapshot.profile() != null ? snapshot.profile() : new ActorProfile("ReplayActor", null, null);
      return new PlayerSpec(profile);
    }
    return new MobSpec(parseActorType(snapshot.entityType()));
  }

  private ActorType parseActorType(String type) {
    try {
      return ActorType.valueOf(type.toUpperCase());
    } catch (IllegalArgumentException e) {
      return ActorType.ZOMBIE;
    }
  }

}
