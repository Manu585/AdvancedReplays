package com.github.manu585.advancedreplays.nms_v1_21_r0.actor;

import com.github.manu585.advancedreplays.api.actor.Actor;
import com.github.manu585.advancedreplays.api.domain.ReplayPosition;
import com.github.manu585.advancedreplays.nms_v1_21_r0.actor.handle.NmsActorHandle;
import com.github.manu585.advancedreplays.nms_v1_21_r0.actor.packet.ActorPacketBridge;
import com.github.manu585.advancedreplays.nms_v1_21_r0.actor.packet.ViewerProvider;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/** Template base class for replay actors, managing handle, packet bridge, and viewer lifecycle. */
public abstract class BaseReplayActor implements Actor {

  protected final ActorPacketBridge actorPacketBridge;
  protected final ViewerProvider viewerProvider;

  protected NmsActorHandle handle;
  protected boolean spawned;

  public BaseReplayActor(ActorPacketBridge actorPacketBridge, ViewerProvider viewerProvider) {
    this.actorPacketBridge = actorPacketBridge;
    this.viewerProvider = viewerProvider;
  }

  protected abstract NmsActorHandle createHandle(ReplayPosition position);

  protected void moveHandle(ReplayPosition position) {
    handle.entity().moveOrInterpolateTo(new Vec3(position.x(), position.y(), position.z()));
    handle.entity().setYRot(position.yaw());
    handle.entity().setXRot(position.pitch());
    handle.entity().setYHeadRot(position.yaw());
  }

  @Override
  public void spawn(ReplayPosition position) {
    if (spawned) {
      return;
    }
    this.handle = createHandle(position);
    this.spawned = true;
    actorPacketBridge.spawn(handle, viewerProvider.viewers());
  }

  @Override
  public void teleport(ReplayPosition position) {
    if (!spawned || handle == null) {
      return;
    }
    moveHandle(position);
    actorPacketBridge.teleport(handle, viewerProvider.viewers());
  }

  @Override
  public void tick() {
    // optional: metadata/equipment updates
  }

  @Override
  public void destroy() {
    if (!spawned || handle == null) {
      return;
    }
    actorPacketBridge.destroy(handle, viewerProvider.viewers());
    this.handle = null;
    this.spawned = false;
  }

  public void setEquipment(EquipmentSlot slot, ItemStack item) {
    if (!spawned || handle == null) return;
    actorPacketBridge.sendEquipment(handle, slot, item, viewerProvider.viewers());
  }

  public void updateMetadata() {
    if (!spawned || handle == null) return;
    actorPacketBridge.sendMetadata(handle, viewerProvider.viewers());
  }

  public void playAnimation(int animationId) {
    if (!spawned || handle == null) return;
    actorPacketBridge.sendAnimation(handle, animationId, viewerProvider.viewers());
  }

  public NmsActorHandle handle() {
    return handle;
  }

  public boolean isSpawned() {
    return spawned;
  }

}
