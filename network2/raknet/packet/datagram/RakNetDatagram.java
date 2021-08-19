package org.crimson.network2.raknet.packet.datagram;

import io.netty.buffer.ByteBuf;
import io.netty.util.AbstractReferenceCounted;
import io.netty.util.ReferenceCounted;
import org.crimson.network2.raknet.packet.encapsulated.EncapsulatedPacket;
import org.crimson.network2.raknet.protocol.RakNetProtocol;

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
     * Create a new {@link RakNetDatagram} from the provided {@code packet}
     *
     * @param packet the packet
     * @return a new {@link RakNetDatagram}
     */
    public static RakNetDatagram ofEncapsulated(EncapsulatedPacket packet) {
        final RakNetDatagram datagram = new RakNetDatagram();
        datagram.packets.add(packet);

        if (packet.isSplit()) {
            datagram.flags |= RakNetProtocol.CONTINUOUS_SEND;
        }
        return datagram;
    }

    /**
     * Decode this datagram packet.
     *
     * @param content the content
     */
    public void decodePacket(ByteBuf content) {
        flags = content.readByte();
        sequence = content.readUnsignedMediumLE();
        while (content.isReadable()) packets.add(EncapsulatedPacket.from(content));
    }

    /**
     * Encode this packet to the provided {@code to}
     *
     * @param to the to
     */
    public void encodePacket(ByteBuf to) {
        to.writeByte(flags);
        to.writeMediumLE(sequence);
        packets.forEach(packet -> packet.encode(to));
    }

    /**
     * @return a list of packets within this datagram.
     */
    public List<EncapsulatedPacket> getPackets() {
        return packets;
    }

    /**
     * The size of this datagram.
     *
     * @return the size
     */
    public int getSize() {
        int size = RakNetProtocol.DATAGRAM_HEADER_SIZE;
        for (EncapsulatedPacket packet : packets) {
            size += packet.getSize();
        }
        return size;
    }

    /**
     * Set flags
     *
     * @param flags the flags
     */
    public void setFlags(byte flags) {
        this.flags = flags;
    }

    /**
     * Set sequence
     *
     * @param sequence the sequence
     */
    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    @Override
    protected void deallocate() {
        packets.forEach(EncapsulatedPacket::release);
        packets.clear();
    }

    @Override
    public ReferenceCounted touch(Object hint) {
        packets.forEach(packet -> packet.touch(hint));
        return this;
    }
}
