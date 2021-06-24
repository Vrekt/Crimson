package org.crimson.network.raknet.packet.datagram;

import io.netty.buffer.ByteBuf;
import io.netty.util.AbstractReferenceCounted;
import io.netty.util.ReferenceCounted;
import org.crimson.network.raknet.packet.encapsulated.EncapsulatedPacket;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a RakNet datagram packet.
 * <p>
 * Reference:
 * https://github.com/CloudburstMC/Network/blob/607c81b9b4194d1bdadb2801eb795ca061081a4f/raknet/src/main/java/com/nukkitx/network/raknet/RakNetDatagram.java
 */
public final class RakNetDatagram extends AbstractReferenceCounted {

    /**
     * Set of packets within this datagram.
     */
    private final List<EncapsulatedPacket> packets = new ArrayList<>();

    /**
     * Flags
     */
    private byte flags;

    /**
     * Sequence index
     */
    private int sequence;

    /**
     * Decode this datagram packet.
     *
     * @param content the content
     */
    public void decodePacket(ByteBuf content) {
        flags = content.readByte();
        sequence = content.readUnsignedMediumLE();
        while (content.isReadable()) {
            final EncapsulatedPacket packet = new EncapsulatedPacket();
            packet.decode(content);
            packets.add(packet);
        }
    }

    /**
     * @return a list of packets within this datagram.
     */
    public List<EncapsulatedPacket> getPackets() {
        return packets;
    }

    @Override
    protected void deallocate() {

    }

    @Override
    public ReferenceCounted touch(Object hint) {
        return null;
    }
}
