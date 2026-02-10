package com.github.manu585.advancedreplays.bukkit.listener;

import com.github.manu585.advancedreplays.core.recording.RecordingSessionManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/** Automatically starts recording on player join and stops on player quit. */
public class PlayerRecordingLifecycle implements Listener {

  private final RecordingSessionManager sessionManager;

  public PlayerRecordingLifecycle(RecordingSessionManager sessionManager) {
    this.sessionManager = sessionManager;
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onPlayerJoin(PlayerJoinEvent event) {
    Player player = event.getPlayer();
    if (!sessionManager.isRecording(player.getUniqueId())) {
      sessionManager.startRecording(
          player.getUniqueId(),
          player.getName(),
          player.getWorld().getName()
      );
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onPlayerQuit(PlayerQuitEvent event) {
    Player player = event.getPlayer();
    if (sessionManager.isRecording(player.getUniqueId())) {
      sessionManager.stopRecording(player.getUniqueId());
    }
  }

}
