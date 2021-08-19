package org.crimson.network2.raknet.packet;

import io.netty.buffer.ByteBuf;

/**
 * Used to create new {@link RakNetPacket}'s
 *
 * @param <T> the type
 */
public interface RakNetPacketFactory<T> {

    /**
     * @return a new packet instance
     */
    T newPacket(ByteBuf content);

}
