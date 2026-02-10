package com.github.manu585.advancedreplays.api.event.codec;

import com.github.manu585.advancedreplays.api.event.*;

import java.util.HashMap;
import java.util.Map;

/** Registry mapping event type identifiers to their corresponding classes for codec lookup. */
public final class EventTypeRegistry {

  private static final Map<Class<? extends ReplayEvent>, Byte> CLASS_TO_ID = new HashMap<>();
  private static final Map<Byte, Class<? extends ReplayEvent>> ID_TO_CLASS = new HashMap<>();

  static {
    register((byte) 1, BlockChangeEvent.class);
    register((byte) 2, BlockInteractEvent.class);
    register((byte) 10, EntitySpawnEvent.class);
    register((byte) 11, EntityDeathEvent.class);
    register((byte) 12, EntityMoveEvent.class);
    register((byte) 13, EntityEquipmentEvent.class);
    register((byte) 14, EntityDamageEvent.class);
    register((byte) 15, EntityMetadataEvent.class);
    register((byte) 20, PlayerSwingEvent.class);
    register((byte) 21, PlayerSneakEvent.class);
    register((byte) 22, PlayerSprintEvent.class);
    register((byte) 30, ChatEvent.class);
    register((byte) 40, WeatherChangeEvent.class);
    register((byte) 41, TimeChangeEvent.class);
    register((byte) 50, InventoryEvent.class);
  }

  private EventTypeRegistry() {}

  private static void register(byte id, Class<? extends ReplayEvent> clazz) {
    CLASS_TO_ID.put(clazz, id);
    ID_TO_CLASS.put(id, clazz);
  }

  public static byte idFor(Class<? extends ReplayEvent> clazz) {
    Byte id = CLASS_TO_ID.get(clazz);
    if (id == null) {
      throw new IllegalArgumentException("Unknown event type: " + clazz.getName());
    }
    return id;
  }

  public static Class<? extends ReplayEvent> classFor(byte id) {
    Class<? extends ReplayEvent> clazz = ID_TO_CLASS.get(id);
    if (clazz == null) {
      throw new IllegalArgumentException("Unknown event type ID: " + id);
    }
    return clazz;
  }

}
