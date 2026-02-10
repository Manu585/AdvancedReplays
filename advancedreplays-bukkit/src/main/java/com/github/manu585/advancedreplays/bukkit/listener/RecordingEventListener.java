package com.github.manu585.advancedreplays.bukkit.listener;

import com.github.manu585.advancedreplays.api.domain.ActorProfile;
import com.github.manu585.advancedreplays.api.domain.BlockPos;
import com.github.manu585.advancedreplays.api.domain.ReplayPosition;
import com.github.manu585.advancedreplays.api.event.*;
import com.github.manu585.advancedreplays.core.recording.RecordingSession;
import com.github.manu585.advancedreplays.core.recording.RecordingSessionManager;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

import java.util.UUID;

/** Bukkit event listener that captures in-game events and routes them to per-player recording sessions. */
public class RecordingEventListener implements Listener {

  private final RecordingSessionManager sessionManager;

  public RecordingEventListener(RecordingSessionManager sessionManager) {
    this.sessionManager = sessionManager;
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onBlockPlace(BlockPlaceEvent event) {
    Player player = event.getPlayer();
    withSession(player.getUniqueId(), session -> {
      Block block = event.getBlock();
      String worldName = block.getWorld().getName();
      session.submitEvent(new BlockChangeEvent(
          session.currentTick(),
          blockPos(block, worldName),
          event.getBlockReplacedState().getType().getKey().toString(),
          block.getType().getKey().toString(),
          player.getEntityId(),
          BlockChangeCause.PLAYER_PLACE
      ));
    });
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onBlockBreak(BlockBreakEvent event) {
    Player player = event.getPlayer();
    withSession(player.getUniqueId(), session -> {
      Block block = event.getBlock();
      String worldName = block.getWorld().getName();
      session.submitEvent(new BlockChangeEvent(
          session.currentTick(),
          blockPos(block, worldName),
          block.getType().getKey().toString(),
          "minecraft:air",
          player.getEntityId(),
          BlockChangeCause.PLAYER_BREAK
      ));
    });
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onEntitySpawn(EntitySpawnEvent event) {
    Entity entity = event.getEntity();
    String worldName = entity.getWorld().getName();
    Location loc = entity.getLocation();
    ActorProfile profile = null;
    if (entity instanceof Player player) {
      profile = new ActorProfile(player.getName(), null, null);
    }
    ActorProfile finalProfile = profile;
    forNearbyPlayers(entity, session -> {
      session.submitEvent(new com.github.manu585.advancedreplays.api.event.EntitySpawnEvent(
          session.currentTick(),
          entity.getEntityId(),
          entity.getType().getKey().toString(),
          toReplayPos(loc, worldName),
          finalProfile
      ));
    });
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onEntityDeath(EntityDeathEvent event) {
    LivingEntity entity = event.getEntity();
    int killerId = entity.getKiller() != null ? entity.getKiller().getEntityId() : -1;
    forNearbyPlayers(entity, session -> {
      session.submitEvent(new com.github.manu585.advancedreplays.api.event.EntityDeathEvent(
          session.currentTick(),
          entity.getEntityId(),
          killerId
      ));
    });
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
    Entity entity = event.getEntity();
    forNearbyPlayers(entity, session -> {
      session.submitEvent(new EntityDamageEvent(
          session.currentTick(),
          entity.getEntityId(),
          event.getDamager().getEntityId(),
          event.getFinalDamage(),
          event.getCause().name()
      ));
    });
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onPlayerMove(PlayerMoveEvent event) {
    Player player = event.getPlayer();
    Location to = event.getTo();
    Location from = event.getFrom();
    if (to.getX() == from.getX() && to.getY() == from.getY() && to.getZ() == from.getZ()
        && to.getYaw() == from.getYaw() && to.getPitch() == from.getPitch()) {
      return;
    }
    String worldName = player.getWorld().getName();
    withSession(player.getUniqueId(), session -> {
      session.submitEvent(new EntityMoveEvent(
          session.currentTick(),
          player.getEntityId(),
          toReplayPos(to, worldName),
          player.isOnGround()
      ));
    });
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
    Player player = event.getPlayer();
    withSession(player.getUniqueId(), session -> {
      session.submitEvent(new PlayerSneakEvent(
          session.currentTick(),
          player.getEntityId(),
          event.isSneaking()
      ));
    });
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onPlayerToggleSprint(PlayerToggleSprintEvent event) {
    Player player = event.getPlayer();
    withSession(player.getUniqueId(), session -> {
      session.submitEvent(new PlayerSprintEvent(
          session.currentTick(),
          player.getEntityId(),
          event.isSprinting()
      ));
    });
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onAsyncChat(AsyncChatEvent event) {
    Player player = event.getPlayer();
    String message = PlainTextComponentSerializer.plainText().serialize(event.message());
    withSession(player.getUniqueId(), session -> {
      session.submitEvent(new ChatEvent(
          session.currentTick(),
          player.getUniqueId(),
          player.getName(),
          message
      ));
    });
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onWeatherChange(WeatherChangeEvent event) {
    String worldName = event.getWorld().getName();
    for (Player player : event.getWorld().getPlayers()) {
      withSession(player.getUniqueId(), session -> {
        session.submitEvent(new com.github.manu585.advancedreplays.api.event.WeatherChangeEvent(
            session.currentTick(),
            event.toWeatherState(),
            event.getWorld().isThundering()
        ));
      });
    }
  }

  private void withSession(UUID playerUuid, java.util.function.Consumer<RecordingSession> action) {
    sessionManager.getSession(playerUuid).ifPresent(action);
  }

  private void forNearbyPlayers(Entity entity, java.util.function.Consumer<RecordingSession> action) {
    for (Player player : entity.getTrackedBy()) {
      withSession(player.getUniqueId(), action);
    }
    if (entity instanceof Player target) {
      withSession(target.getUniqueId(), action);
    }
  }

  private static BlockPos blockPos(Block block, String worldName) {
    return new BlockPos(worldName, block.getX(), block.getY(), block.getZ());
  }

  private static ReplayPosition toReplayPos(Location loc, String worldName) {
    return new ReplayPosition(worldName, loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
  }

}
