package com.github.manu585.advancedreplays.nms_v1_21_r0.actor.util;

import com.github.manu585.advancedreplays.api.domain.ActorProfile;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

/** Utility for creating Mojang GameProfile instances for fake player actors. */
public class GameProfiles {

  private GameProfiles() {}

  public static GameProfile from(ActorProfile actorProfile) {
    Objects.requireNonNull(actorProfile, "actorProfile");

    UUID uuid = uuidFrom(actorProfile);
    String name = sanitizeName(actorProfile.displayName());

    GameProfile gameProfile = new GameProfile(uuid, name);

    String texture = trimToNull(actorProfile.skinTexture());
    String signature = trimToNull(actorProfile.skinSignature());

    if (texture != null && signature != null) {
      gameProfile.properties().put("textures", new Property("textures", texture, signature));
    }

    return gameProfile;
  }

  private static UUID uuidFrom(ActorProfile actorProfile) {
    String key =  (actorProfile.displayName() == null ? "" : actorProfile.displayName() + "|" +
                  (actorProfile.skinTexture() == null ? "" : actorProfile.skinTexture()) + "|" +
                  (actorProfile.skinSignature() == null ? "" : actorProfile.skinSignature()));
    return UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8));
  }

  private static String sanitizeName(String raw) {
    String fallback = "ReplayActor";
    if (raw == null || raw.isBlank()) {
      return fallback;
    }

    String cleaned = raw.replaceAll("[^A-Za-z0-9_]", "");
    if (cleaned.isBlank()) {
      cleaned = fallback;
    }

    if (cleaned.length() > 16) {
      cleaned = cleaned.substring(0, 16);
    }

    return cleaned;
  }

  private static String trimToNull(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

}
