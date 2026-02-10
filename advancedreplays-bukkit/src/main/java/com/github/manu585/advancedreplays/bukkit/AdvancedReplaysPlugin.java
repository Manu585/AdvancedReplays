package com.github.manu585.advancedreplays.bukkit;

import com.github.manu585.advancedreplays.bukkit.command.ReplayCommand;
import com.github.manu585.advancedreplays.bukkit.listener.EntityMovementPoller;
import com.github.manu585.advancedreplays.bukkit.listener.PlayerRecordingLifecycle;
import com.github.manu585.advancedreplays.bukkit.listener.RecordingEventListener;
import com.github.manu585.advancedreplays.core.NmsProvider;
import com.github.manu585.advancedreplays.core.bootstrap.ReplaysBootstrap;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/** Plugin entry point for AdvancedReplays, wiring together recording, playback, and NMS subsystems. */
public class AdvancedReplaysPlugin extends JavaPlugin {

  private static final String NMS_BOOTSTRAP_CLASS = "com.github.manu585.advancedreplays.nms_v1_21_r0.NmsBootstrap_V1_21_R0";

  private ReplaysBootstrap bootstrap;

  @Override
  public void onLoad() {
    bootstrap = new ReplaysBootstrap(getDataFolder().toPath());
    bootstrap.onLoad();
  }

  @Override
  public void onEnable() {
    bootstrap.onEnable();

    NmsProvider nmsProvider;
    try {
      nmsProvider = (NmsProvider) Class.forName(NMS_BOOTSTRAP_CLASS)
          .getDeclaredConstructor()
          .newInstance();
    } catch (ReflectiveOperationException e) {
      getLogger().severe("Failed to initialize NMS provider: " + e.getMessage());
      getServer().getPluginManager().disablePlugin(this);
      return;
    }

    bootstrap.initPlayback(nmsProvider.createActorFactory());
    bootstrap.playbackManager().setWorldEffectHandler(nmsProvider.createWorldEffectHandler());

    getServer().getPluginManager().registerEvents(
        new RecordingEventListener(bootstrap.recordingManager()), this
    );

    getServer().getPluginManager().registerEvents(
        new PlayerRecordingLifecycle(bootstrap.recordingManager()), this
    );

    var movementPoller = new EntityMovementPoller(bootstrap.recordingManager());
    Bukkit.getScheduler().runTaskTimer(this, movementPoller, 1L, 1L);

    Bukkit.getScheduler().runTaskTimer(this, () -> {
      bootstrap.recordingManager().tickAll();
      bootstrap.playbackManager().tickAll();
    }, 1L, 1L);

    var replayCommand = new ReplayCommand(bootstrap.playbackManager(), bootstrap.storageBackend());
    this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
      event.registrar().register("replay", "Control the replay system", replayCommand);
    });

    // Start recording for already-online players (handles /reload)
    for (Player player : Bukkit.getOnlinePlayers()) {
      if (!bootstrap.recordingManager().isRecording(player.getUniqueId())) {
        bootstrap.recordingManager().startRecording(
            player.getUniqueId(),
            player.getName(),
            player.getWorld().getName()
        );
      }
    }

    getLogger().info("AdvancedReplays enabled");
  }

  @Override
  public void onDisable() {
    bootstrap.onDisable();
    getLogger().info("AdvancedReplays disabled");
  }

}
