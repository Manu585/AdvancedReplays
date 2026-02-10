package com.github.manu585.advancedreplays.api.bootstrap;

/**
 * Lifecycle interface for bootstrapping the AdvancedReplays system.
 * Implementations handle initialization, startup, and shutdown phases.
 */
public interface Bootstrap {

  void onLoad();

  void onEnable();

  void onDisable();

}
