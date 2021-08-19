package org.crimson.network2.raknet.packet.server;

import org.crimson.network2.raknet.packet.RakNetPacket;
import org.crimson.network2.raknet.protocol.RakNetProtocol;
import org.crimson.network2.raknet.packet.client.ClientUnconnectedPing;

/**
 * Sent in response to {@link ClientUnconnectedPing}
 * <p>
 * TODO: Could just pre-allocate buffer.
 */
public final class ServerUnconnectedPong extends RakNetPacket {

    /**
     * The ping time
     * The server GUID.
     */
    private final long time, guid;

    /**
     * The MOTD.
     */
    private final String motd;

    /**
     * Initialize
     *
     * @param time the client time
     * @param guid the server GUID
     * @param motd the server MOTD.
     */
    public ServerUnconnectedPong(long time, long guid, String motd) {
        super(true);
        this.time = time;
        this.guid = guid;
        this.motd = motd;
    }

    @Override
    public byte getId() {
        return RakNetProtocol.UNCONNECTED_PONG;
    }

    @Override
    public void decode() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void encode() {
        writeId();
        writeLong(time);
        writeLong(guid);
        writeMagic();
        writeString(motd);
    }
}
