package com.github.manu585.advancedreplays.api.domain;

/** Represents an immutable block position within a specific world. */
public record BlockPos(String worldName, int x, int y, int z) {

}
