package org.crimson.network.raknet.packet.client;

import io.netty.buffer.ByteBuf;
import org.crimson.network.raknet.packet.RakNetPacket;
import org.crimson.network.raknet.protocol.RakNetProtocol;

import java.util.Arrays;

/**
 * The first stage of trying to connect a new client.
 */
public final class ClientOpenConnectionRequest1 extends RakNetPacket {

    /**
     * Magic
     */
    private byte[] magic;

    /**
     * The RakNet protocol version
     */
    private int protocol;

    public ClientOpenConnectionRequest1(ByteBuf packet) {
        super(packet);

        decode();
    }

    @Override
    public byte getId() {
        return RakNetProtocol.OPEN_CONNECTION_REQUEST_1;
    }

    @Override
    public void decode() {
        magic = readMagic();
        protocol = readByte();
    }

    @Override
    public void encode() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean verifyMagic() {
        return Arrays.equals(magic, RakNetProtocol.MAGIC);
    }

    @Override
    public boolean verifyProtocolVersion() {
        return protocol == RakNetProtocol.PROTOCOL_VERSION;
    }
}
