package org.crimson.v3.raknet.packet;

import io.netty.buffer.ByteBuf;

/**
 * Basic RakMessage impl.
 */
public record RakNetMessage(int id, ByteBuf buffer) {
}
