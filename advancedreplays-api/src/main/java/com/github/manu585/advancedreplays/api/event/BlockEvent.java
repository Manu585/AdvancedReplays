package com.github.manu585.advancedreplays.api.event;

/** Marker interface for replay events related to blocks. */
public sealed interface BlockEvent extends ReplayEvent permits
    BlockChangeEvent,
    BlockInteractEvent {

}
