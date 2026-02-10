package com.github.manu585.advancedreplays.bukkit.listener;

import com.github.manu585.advancedreplays.api.domain.ReplayPosition;
import com.github.manu585.advancedreplays.api.event.EntityMoveEvent;
import com.github.manu585.advancedreplays.core.recording.RecordingSession;
import com.github.manu585.advancedreplays.core.recording.RecordingSessionManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/** Polls non-player entity movement each tick and submits position changes to nearby players' recording sessions. */
public class EntityMovementPoller implements Runnable {

  private final RecordingSessionManager sessionManager;
  private final Map<Integer, Location> lastPositions = new HashMap<>();

  public EntityMovementPoller(RecordingSessionManager sessionManager) {
    this.sessionManager = sessionManager;
  }

  @Override
  public void run() {
    for (World world : Bukkit.getWorlds()) {
      String worldName = world.getName();

      for (Entity entity : world.getEntities()) {
        if (entity instanceof Player) continue;

        int entityId = entity.getEntityId();
        Location current = entity.getLocation();
        Location last = lastPositions.get(entityId);

        if (last != null && last.getX() == current.getX() && last.getY() == current.getY()
            && last.getZ() == current.getZ() && last.getYaw() == current.getYaw()
            && last.getPitch() == current.getPitch()) {
          continue;
        }

        lastPositions.put(entityId, current.clone());

        for (Player player : entity.getTrackedBy()) {
          sessionManager.getSession(player.getUniqueId()).ifPresent(session -> {
            if (session.isRecording()) {
              session.submitEvent(new EntityMoveEvent(
                  session.currentTick(),
                  entityId,
                  new ReplayPosition(worldName, current.getX(), current.getY(), current.getZ(),
                      current.getYaw(), current.getPitch()),
                  entity.isOnGround()
              ));
            }
          });
        }
      }
    }
  }

}
