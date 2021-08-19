package org.crimson.network2.raknet.packet.client;


import io.netty.buffer.ByteBuf;
import org.crimson.network2.raknet.packet.RakNetPacket;
import org.crimson.network2.raknet.protocol.RakNetProtocol;

import java.util.Arrays;

/**
 * Sent by clients to retrieve the status or MOTD of the server.
 * <p>
 * Clients will also send their GUID, which is currently un-used.
 */
public final class ClientUnconnectedPing extends RakNetPacket {

    /**
     * The ping time
     */
    private long time;
    /**
     * Magic value
     */
    private byte[] magic;

    public ClientUnconnectedPing(ByteBuf packet) {
        super(packet);

        decode();
    }

    @Override
    public byte getId() {
        return RakNetProtocol.UNCONNECTED_PING;
    }

    @Override
    public void decode() {
        time = readLong();
        magic = readMagic();
    }

    @Override
    public void encode() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean verifyMagic() {
        return Arrays.equals(magic, RakNetProtocol.MAGIC);
    }

    /**
     * @return the ping time.
     */
    public long time() {
        return time;
    }
}
