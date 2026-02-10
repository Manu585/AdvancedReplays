package com.github.manu585.advancedreplays.bukkit.command;

import com.github.manu585.advancedreplays.api.storage.StorageBackend;
import com.github.manu585.advancedreplays.api.timeline.ReplayMetadata;
import com.github.manu585.advancedreplays.bukkit.util.Messages;
import com.github.manu585.advancedreplays.core.playback.PlaybackSession;
import com.github.manu585.advancedreplays.core.playback.PlaybackSessionManager;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/** Command handler for replay playback operations, formatted with MiniMessage. */
@SuppressWarnings("UnstableApiUsage")
public class ReplayCommand implements BasicCommand {

  private final PlaybackSessionManager playbackManager;
  private final StorageBackend storageBackend;

  public ReplayCommand(PlaybackSessionManager playbackManager, StorageBackend storageBackend) {
    this.playbackManager = playbackManager;
    this.storageBackend = storageBackend;
  }

  @Override
  public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {
    CommandSender sender = stack.getSender();

    if (args.length == 0) {
      sendUsage(sender);
      return;
    }

    String sub = args[0].toLowerCase();
    switch (sub) {
      case "play" -> handlePlay(sender, args);
      case "pause" -> handlePause(sender);
      case "resume" -> handleResume(sender);
      case "seek" -> handleSeek(sender, args);
      case "speed" -> handleSpeed(sender, args);
      case "step" -> handleStep(sender, args);
      case "list" -> handleList(sender);
      case "delete" -> handleDelete(sender, args);
      case "stop" -> handleStop(sender);
      default -> sendUsage(sender);
    }
  }

  @Override
  public @NotNull Collection<String> suggest(@NotNull CommandSourceStack stack, @NotNull String[] args) {
    if (args.length <= 1) {
      return List.of("play", "pause", "resume", "seek", "speed", "step", "list", "delete", "stop");
    }
    if (args.length == 2) {
      return switch (args[0].toLowerCase()) {
        case "step" -> List.of("forward", "backward");
        default -> List.of();
      };
    }
    return List.of();
  }

  private void handlePlay(CommandSender sender, String[] args) {
    if (args.length < 2) {
      sender.sendMessage(Messages.error("Usage: /replay play <id>"));
      return;
    }

    try {
      UUID replayId = UUID.fromString(args[1]);
      // Find the replay to get its playerUuid
      List<ReplayMetadata> allReplays = storageBackend.listReplays();
      ReplayMetadata target = allReplays.stream()
          .filter(m -> m.replayId().equals(replayId))
          .findFirst()
          .orElse(null);

      if (target == null) {
        sender.sendMessage(Messages.error("Replay not found."));
        return;
      }

      UUID playerUuid = target.playerUuid() != null ? target.playerUuid() : replayId;
      PlaybackSession session = playbackManager.startPlayback(playerUuid, replayId);
      sender.sendMessage(Messages.success("Playback started. Total ticks: " + session.totalTicks()));
    } catch (IllegalArgumentException e) {
      sender.sendMessage(Messages.error("Invalid replay ID format."));
    } catch (IOException e) {
      sender.sendMessage(Messages.error("Failed to start playback: " + e.getMessage()));
    }
  }

  private void handlePause(CommandSender sender) {
    PlaybackSession session = playbackManager.getAnySession();
    if (session == null) {
      sender.sendMessage(Messages.error("No active playback."));
      return;
    }
    session.pause();
    sender.sendMessage(Messages.info("Playback paused at tick " + session.currentTick()));
  }

  private void handleResume(CommandSender sender) {
    PlaybackSession session = playbackManager.getAnySession();
    if (session == null) {
      sender.sendMessage(Messages.error("No active playback."));
      return;
    }
    session.resume();
    sender.sendMessage(Messages.success("Playback resumed."));
  }

  private void handleSeek(CommandSender sender, String[] args) {
    if (args.length < 2) {
      sender.sendMessage(Messages.error("Usage: /replay seek <tick>"));
      return;
    }
    PlaybackSession session = playbackManager.getAnySession();
    if (session == null) {
      sender.sendMessage(Messages.error("No active playback."));
      return;
    }
    try {
      int tick = Integer.parseInt(args[1]);
      session.seekTo(tick);
      sender.sendMessage(Messages.success("Seeked to tick " + tick));
    } catch (NumberFormatException e) {
      sender.sendMessage(Messages.error("Invalid tick number."));
    }
  }

  private void handleSpeed(CommandSender sender, String[] args) {
    if (args.length < 2) {
      sender.sendMessage(Messages.error("Usage: /replay speed <multiplier>"));
      return;
    }
    PlaybackSession session = playbackManager.getAnySession();
    if (session == null) {
      sender.sendMessage(Messages.error("No active playback."));
      return;
    }
    try {
      double speed = Double.parseDouble(args[1]);
      session.setSpeed(speed);
      sender.sendMessage(Messages.success("Playback speed set to " + speed + "x"));
    } catch (NumberFormatException e) {
      sender.sendMessage(Messages.error("Invalid speed value."));
    }
  }

  private void handleStep(CommandSender sender, String[] args) {
    if (args.length < 2) {
      sender.sendMessage(Messages.error("Usage: /replay step <forward|backward>"));
      return;
    }
    PlaybackSession session = playbackManager.getAnySession();
    if (session == null) {
      sender.sendMessage(Messages.error("No active playback."));
      return;
    }
    switch (args[1].toLowerCase()) {
      case "forward" -> {
        session.stepForward();
        sender.sendMessage(Messages.info("Stepped to tick " + session.currentTick()));
      }
      case "backward" -> {
        session.stepBackward();
        sender.sendMessage(Messages.info("Stepped to tick " + session.currentTick()));
      }
      default -> sender.sendMessage(Messages.error("Usage: /replay step <forward|backward>"));
    }
  }

  private void handleList(CommandSender sender) {
    try {
      List<ReplayMetadata> replays = storageBackend.listReplays();
      if (replays.isEmpty()) {
        sender.sendMessage(Messages.info("No replays found."));
        return;
      }
      sender.sendMessage(Messages.replayListHeader(replays.size()));
      for (ReplayMetadata meta : replays) {
        sender.sendMessage(Messages.replayListEntry(meta));
      }
    } catch (IOException e) {
      sender.sendMessage(Messages.error("Failed to list replays: " + e.getMessage()));
    }
  }

  private void handleDelete(CommandSender sender, String[] args) {
    if (args.length < 2) {
      sender.sendMessage(Messages.error("Usage: /replay delete <id>"));
      return;
    }
    try {
      UUID replayId = UUID.fromString(args[1]);
      // Find the replay to get its playerUuid
      List<ReplayMetadata> allReplays = storageBackend.listReplays();
      ReplayMetadata target = allReplays.stream()
          .filter(m -> m.replayId().equals(replayId))
          .findFirst()
          .orElse(null);

      if (target == null) {
        sender.sendMessage(Messages.error("Replay not found."));
        return;
      }

      UUID playerUuid = target.playerUuid() != null ? target.playerUuid() : replayId;
      boolean deleted = storageBackend.deleteReplay(playerUuid, replayId);
      sender.sendMessage(deleted ? Messages.success("Replay deleted.") : Messages.error("Replay not found."));
    } catch (IllegalArgumentException e) {
      sender.sendMessage(Messages.error("Invalid replay ID format."));
    } catch (IOException e) {
      sender.sendMessage(Messages.error("Failed to delete replay: " + e.getMessage()));
    }
  }

  private void handleStop(CommandSender sender) {
    PlaybackSession session = playbackManager.getAnySession();
    if (session == null) {
      sender.sendMessage(Messages.error("No active playback."));
      return;
    }
    session.destroy();
    sender.sendMessage(Messages.success("Playback stopped."));
  }

  private void sendUsage(CommandSender sender) {
    sender.sendMessage(Messages.info("Usage: /replay <play|pause|resume|seek|speed|step|list|delete|stop>"));
  }

}
