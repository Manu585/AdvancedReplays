package com.github.manu585.advancedreplays.api.event.codec;

import com.github.manu585.advancedreplays.api.event.ReplayEvent;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/** Codec for encoding and decoding replay events to and from binary streams. */
public interface ReplayEventCodec {

  void encode(ReplayEvent event, DataOutput out) throws IOException;

  ReplayEvent decode(DataInput in) throws IOException;

}
