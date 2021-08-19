package org.crimson.network2.raknet.packet.client;

import io.netty.buffer.ByteBuf;
import org.crimson.network2.raknet.packet.RakNetPacket;
import org.crimson.network2.raknet.protocol.RakNetProtocol;

import java.net.InetSocketAddress;
import java.util.Arrays;

/**
 * The second stage of connection
 */
public final class ClientOpenConnectionRequest2 extends RakNetPacket {

    /**
     * Magic.
     */
    private byte[] magic;

    /**
     * Address
     */
    private InetSocketAddress address;

    /**
     * MTU size.
     */
    private int mtu;

    /**
     * Client GUID
     */
    private long guid;

    public ClientOpenConnectionRequest2(ByteBuf packet) {
        super(packet);

        decode();
    }

    @Override
    public byte getId() {
        return RakNetProtocol.OPEN_CONNECTION_REQUEST_2;
    }

    @Override
    public void decode() {
        magic = readMagic();
        address = readAddress();
        mtu = readShort();
        guid = readLong();
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
     * @return the server address
     */
    public InetSocketAddress getAddress() {
        return address;
    }

    /**
     * @return the MTU size.
     */
    public int getMtu() {
        return mtu;
    }

    /**
     * @return the client GUID.
     */
    public long getGuid() {
        return guid;
    }
}
