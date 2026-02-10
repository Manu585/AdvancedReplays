package com.github.manu585.advancedreplays.api.event;

/** Sealed base interface for all replay events, each carrying a tick timestamp. */
public sealed interface ReplayEvent permits
    BlockEvent,
    EntityEvent,
    PlayerActionEvent,
    ChatEvent,
    WorldEvent,
    InventoryEvent {

  int tick();

}
