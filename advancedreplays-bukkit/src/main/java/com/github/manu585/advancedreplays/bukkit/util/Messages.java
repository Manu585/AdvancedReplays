package com.github.manu585.advancedreplays.bukkit.util;

import com.github.manu585.advancedreplays.api.timeline.ReplayMetadata;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/** Centralized MiniMessage formatting utility for all plugin chat output. */
public final class Messages {

  private static final MiniMessage MM = MiniMessage.miniMessage();
  private static final String PREFIX = "<dark_gray>[<gradient:gold:yellow>AdvancedReplays</gradient>]</dark_gray> ";
  private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
      .withZone(ZoneId.systemDefault());

  private Messages() {}

  public static Component error(String message) {
    return MM.deserialize(PREFIX + "<red>" + message + "</red>");
  }

  public static Component success(String message) {
    return MM.deserialize(PREFIX + "<green>" + message + "</green>");
  }

  public static Component info(String message) {
    return MM.deserialize(PREFIX + "<gray>" + message + "</gray>");
  }

  public static Component replayListEntry(ReplayMetadata meta) {
    String playerInfo = meta.playerName() != null ? meta.playerName() : "unknown";
    String partInfo = meta.partIndex() > 0 ? " (part " + meta.partIndex() + ")" : "";
    String line = "<gray> - </gray><yellow>" + meta.replayId().toString().substring(0, 8) + "...</yellow>"
        + " <dark_gray>|</dark_gray> <white>" + playerInfo + "</white>"
        + " <dark_gray>|</dark_gray> <aqua>" + meta.worldName() + "</aqua>"
        + " <dark_gray>|</dark_gray> <green>" + meta.durationTicks() + " ticks</green>"
        + " <dark_gray>|</dark_gray> <gray>" + TIME_FMT.format(meta.startTimestamp()) + "</gray>"
        + partInfo;

    String hoverText = "<yellow>Replay ID:</yellow> " + meta.replayId()
        + "\n<yellow>Session:</yellow> " + (meta.sessionId() != null ? meta.sessionId() : "N/A")
        + "\n<yellow>Part:</yellow> " + meta.partIndex()
        + "\n<yellow>Player:</yellow> " + playerInfo
        + (meta.playerUuid() != null ? " (" + meta.playerUuid() + ")" : "")
        + "\n<gray>Click to play</gray>";

    Component hover = MM.deserialize(hoverText);
    return MM.deserialize(line)
        .hoverEvent(HoverEvent.showText(hover))
        .clickEvent(ClickEvent.suggestCommand("/replay play " + meta.replayId()));
  }

  public static Component replayListHeader(int count) {
    return MM.deserialize(PREFIX + "<yellow>Replays (<white>" + count + "</white>):</yellow>");
  }

}
