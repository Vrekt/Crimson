package org.crimson.network2.raknet.packet.client;

import io.netty.buffer.ByteBuf;
import org.crimson.network2.raknet.packet.RakNetPacket;
import org.crimson.network2.raknet.protocol.RakNetProtocol;

/**
 * A client connection request, sent after all replies.
 */
public final class ClientConnectionRequest extends RakNetPacket {

    /**
     * GUID and time.
     */
    private long guid, time;

    /**
     * Security/encryption flag.
     */
    private boolean security;

    public ClientConnectionRequest(ByteBuf packet) {
        super(packet);

        decode();
    }

    @Override
    public byte getId() {
        return RakNetProtocol.CONNECTION_REQUEST;
    }

    @Override
    public void decode() {
        this.guid = readLong();
        this.time = readLong();
        this.security = readBoolean();
    }

    @Override
    public void encode() {
        throw new UnsupportedOperationException();
    }

    /**
     * @return client GUID
     */
    public long getGuid() {
        return guid;
    }

    /**
     * @return client time.
     */
    public long getTime() {
        return time;
    }

    /**
     * @return security flag.
     */
    public boolean isSecurity() {
        return security;
    }
}
