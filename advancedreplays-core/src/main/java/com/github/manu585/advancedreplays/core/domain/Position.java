package com.github.manu585.advancedreplays.core.domain;

public class Position {

  private String worldName;
  private double x;
  private double y;
  private double z;
  private float yaw;
  private float pitch;

  public String getWorldName() {
    return worldName;
  }

  public double getX() {
    return x;
  }

  public double getY() {
    return y;
  }

  public double getZ() {
    return z;
  }

  public float getYaw() {
    return yaw;
  }

  public float getPitch() {
    return pitch;
  }

  public void setWorldName(String worldName) {
    this.worldName = worldName;
  }

  public void setX(double x) {
    this.x = x;
  }

  public void setY(double y) {
    this.y = y;
  }

  public void setZ(double z) {
    this.z = z;
  }

  public void setYaw(float yaw) {
    this.yaw = yaw;
  }

  public void setPitch(float pitch) {
    this.pitch = pitch;
  }

}
