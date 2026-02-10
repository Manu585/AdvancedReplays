package com.github.manu585.advancedreplays.core.storage;

import com.github.manu585.advancedreplays.api.domain.ActorProfile;
import com.github.manu585.advancedreplays.api.domain.BlockPos;
import com.github.manu585.advancedreplays.api.domain.ReplayPosition;
import com.github.manu585.advancedreplays.api.event.*;
import com.github.manu585.advancedreplays.api.event.codec.EventTypeRegistry;
import com.github.manu585.advancedreplays.api.event.codec.ReplayEventCodec;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.UUID;

/** Binary codec that serializes and deserializes replay events to compact binary format. */
public final class BinaryReplayEventCodec implements ReplayEventCodec {

  @Override
  public void encode(ReplayEvent event, DataOutput out) throws IOException {
    byte typeId = EventTypeRegistry.idFor(event.getClass());
    out.writeByte(typeId);
    out.writeInt(event.tick());

    switch (event) {
      case BlockChangeEvent e -> encodeBlockChange(e, out);
      case BlockInteractEvent e -> encodeBlockInteract(e, out);
      case EntitySpawnEvent e -> encodeEntitySpawn(e, out);
      case EntityDeathEvent e -> encodeEntityDeath(e, out);
      case EntityMoveEvent e -> encodeEntityMove(e, out);
      case EntityEquipmentEvent e -> encodeEntityEquipment(e, out);
      case EntityDamageEvent e -> encodeEntityDamage(e, out);
      case EntityMetadataEvent e -> throw new IOException("EntityMetadataEvent encoding not yet implemented");
      case PlayerSwingEvent e -> out.writeInt(e.entityId());
      case PlayerSneakEvent e -> { out.writeInt(e.entityId()); out.writeBoolean(e.sneaking()); }
      case PlayerSprintEvent e -> { out.writeInt(e.entityId()); out.writeBoolean(e.sprinting()); }
      case ChatEvent e -> encodeChatEvent(e, out);
      case WeatherChangeEvent e -> { out.writeBoolean(e.raining()); out.writeBoolean(e.thundering()); }
      case TimeChangeEvent e -> out.writeLong(e.worldTime());
      case InventoryEvent e -> throw new IOException("InventoryEvent encoding not yet implemented");
    }
  }

  @Override
  public ReplayEvent decode(DataInput in) throws IOException {
    byte typeId = in.readByte();
    int tick = in.readInt();
    Class<? extends ReplayEvent> clazz = EventTypeRegistry.classFor(typeId);

    if (clazz == BlockChangeEvent.class) return decodeBlockChange(tick, in);
    if (clazz == BlockInteractEvent.class) return decodeBlockInteract(tick, in);
    if (clazz == EntitySpawnEvent.class) return decodeEntitySpawn(tick, in);
    if (clazz == EntityDeathEvent.class) return decodeEntityDeath(tick, in);
    if (clazz == EntityMoveEvent.class) return decodeEntityMove(tick, in);
    if (clazz == EntityEquipmentEvent.class) return decodeEntityEquipment(tick, in);
    if (clazz == EntityDamageEvent.class) return decodeEntityDamage(tick, in);
    if (clazz == PlayerSwingEvent.class) return new PlayerSwingEvent(tick, in.readInt());
    if (clazz == PlayerSneakEvent.class) return new PlayerSneakEvent(tick, in.readInt(), in.readBoolean());
    if (clazz == PlayerSprintEvent.class) return new PlayerSprintEvent(tick, in.readInt(), in.readBoolean());
    if (clazz == ChatEvent.class) return decodeChatEvent(tick, in);
    if (clazz == WeatherChangeEvent.class) return new WeatherChangeEvent(tick, in.readBoolean(), in.readBoolean());
    if (clazz == TimeChangeEvent.class) return new TimeChangeEvent(tick, in.readLong());

    throw new IOException("Cannot decode event type: " + clazz.getName());
  }

  // --- Block events ---

  private void encodeBlockChange(BlockChangeEvent e, DataOutput out) throws IOException {
    writeBlockPos(e.blockPos(), out);
    out.writeUTF(e.previousType());
    out.writeUTF(e.newType());
    out.writeInt(e.actorId());
    out.writeByte(e.cause().ordinal());
  }

  private BlockChangeEvent decodeBlockChange(int tick, DataInput in) throws IOException {
    BlockPos pos = readBlockPos(in);
    String prev = in.readUTF();
    String next = in.readUTF();
    int actorId = in.readInt();
    BlockChangeCause cause = BlockChangeCause.values()[in.readByte()];
    return new BlockChangeEvent(tick, pos, prev, next, actorId, cause);
  }

  private void encodeBlockInteract(BlockInteractEvent e, DataOutput out) throws IOException {
    writeBlockPos(e.blockPos(), out);
    out.writeInt(e.actorId());
    out.writeUTF(e.interactionType());
  }

  private BlockInteractEvent decodeBlockInteract(int tick, DataInput in) throws IOException {
    BlockPos pos = readBlockPos(in);
    int actorId = in.readInt();
    String type = in.readUTF();
    return new BlockInteractEvent(tick, pos, actorId, type);
  }

  // --- Entity events ---

  private void encodeEntitySpawn(EntitySpawnEvent e, DataOutput out) throws IOException {
    out.writeInt(e.entityId());
    out.writeUTF(e.entityType());
    writePosition(e.position(), out);
    writeProfile(e.profile(), out);
  }

  private EntitySpawnEvent decodeEntitySpawn(int tick, DataInput in) throws IOException {
    int entityId = in.readInt();
    String entityType = in.readUTF();
    ReplayPosition pos = readPosition(in);
    ActorProfile profile = readProfile(in);
    return new EntitySpawnEvent(tick, entityId, entityType, pos, profile);
  }

  private void encodeEntityDeath(EntityDeathEvent e, DataOutput out) throws IOException {
    out.writeInt(e.entityId());
    out.writeInt(e.killerId());
  }

  private EntityDeathEvent decodeEntityDeath(int tick, DataInput in) throws IOException {
    return new EntityDeathEvent(tick, in.readInt(), in.readInt());
  }

  private void encodeEntityMove(EntityMoveEvent e, DataOutput out) throws IOException {
    out.writeInt(e.entityId());
    writePosition(e.position(), out);
    out.writeBoolean(e.onGround());
  }

  private EntityMoveEvent decodeEntityMove(int tick, DataInput in) throws IOException {
    int entityId = in.readInt();
    ReplayPosition pos = readPosition(in);
    boolean onGround = in.readBoolean();
    return new EntityMoveEvent(tick, entityId, pos, onGround);
  }

  private void encodeEntityEquipment(EntityEquipmentEvent e, DataOutput out) throws IOException {
    out.writeInt(e.entityId());
    out.writeUTF(e.slot());
    out.writeUTF(e.itemType());
    byte[] data = e.itemData();
    out.writeInt(data != null ? data.length : 0);
    if (data != null && data.length > 0) {
      out.write(data);
    }
  }

  private EntityEquipmentEvent decodeEntityEquipment(int tick, DataInput in) throws IOException {
    int entityId = in.readInt();
    String slot = in.readUTF();
    String itemType = in.readUTF();
    int dataLen = in.readInt();
    byte[] data = dataLen > 0 ? new byte[dataLen] : null;
    if (data != null) {
      in.readFully(data);
    }
    return new EntityEquipmentEvent(tick, entityId, slot, itemType, data);
  }

  private void encodeEntityDamage(EntityDamageEvent e, DataOutput out) throws IOException {
    out.writeInt(e.entityId());
    out.writeInt(e.attackerId());
    out.writeDouble(e.damage());
    out.writeUTF(e.damageSource());
  }

  private EntityDamageEvent decodeEntityDamage(int tick, DataInput in) throws IOException {
    int entityId = in.readInt();
    int attackerId = in.readInt();
    double damage = in.readDouble();
    String source = in.readUTF();
    return new EntityDamageEvent(tick, entityId, attackerId, damage, source);
  }

  // --- Chat ---

  private void encodeChatEvent(ChatEvent e, DataOutput out) throws IOException {
    out.writeLong(e.senderUuid().getMostSignificantBits());
    out.writeLong(e.senderUuid().getLeastSignificantBits());
    out.writeUTF(e.senderName());
    out.writeUTF(e.message());
  }

  private ChatEvent decodeChatEvent(int tick, DataInput in) throws IOException {
    UUID uuid = new UUID(in.readLong(), in.readLong());
    String name = in.readUTF();
    String message = in.readUTF();
    return new ChatEvent(tick, uuid, name, message);
  }

  // --- Helpers ---

  private void writeBlockPos(BlockPos pos, DataOutput out) throws IOException {
    out.writeUTF(pos.worldName());
    out.writeInt(pos.x());
    out.writeInt(pos.y());
    out.writeInt(pos.z());
  }

  private BlockPos readBlockPos(DataInput in) throws IOException {
    return new BlockPos(in.readUTF(), in.readInt(), in.readInt(), in.readInt());
  }

  private void writePosition(ReplayPosition pos, DataOutput out) throws IOException {
    out.writeUTF(pos.worldName());
    out.writeDouble(pos.x());
    out.writeDouble(pos.y());
    out.writeDouble(pos.z());
    out.writeFloat(pos.yaw());
    out.writeFloat(pos.pitch());
  }

  private ReplayPosition readPosition(DataInput in) throws IOException {
    return new ReplayPosition(in.readUTF(), in.readDouble(), in.readDouble(), in.readDouble(), in.readFloat(), in.readFloat());
  }

  private void writeProfile(ActorProfile profile, DataOutput out) throws IOException {
    boolean hasProfile = profile != null;
    out.writeBoolean(hasProfile);
    if (hasProfile) {
      out.writeUTF(profile.displayName() != null ? profile.displayName() : "");
      out.writeUTF(profile.skinTexture() != null ? profile.skinTexture() : "");
      out.writeUTF(profile.skinSignature() != null ? profile.skinSignature() : "");
    }
  }

  private ActorProfile readProfile(DataInput in) throws IOException {
    if (!in.readBoolean()) return null;
    String name = in.readUTF();
    String texture = in.readUTF();
    String signature = in.readUTF();
    return new ActorProfile(
        name.isEmpty() ? null : name,
        texture.isEmpty() ? null : texture,
        signature.isEmpty() ? null : signature
    );
  }

}
