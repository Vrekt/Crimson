package org.crimson.v3.raknet.packet;

import io.netty.buffer.ByteBuf;
import io.netty.util.AbstractReferenceCounted;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.ReferenceCounted;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a datagram message that is RakNet.
 */
public final class RakNetDatagram extends AbstractReferenceCounted {

    /**
     * Set of packets within this datagram.
     */
    private final List<RakEncapsulatedPacket> packets = new ArrayList<>();

    /**
     * The sequence of this datagram.
     */
    private int sequence;

    /**
     * Decode this datagram.
     *
     * @return the result.
     */
    public boolean decode(ByteBuf content) {
        try {
            sequence = content.readUnsignedMediumLE();
            while (content.isReadable()) {
                final RakEncapsulatedPacket packet = new RakEncapsulatedPacket();
                if (!packet.decode(content)) return false;

                packets.add(packet.retain());
            }
        } catch (Exception any) {
            any.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * Retrieve a list of packets contained within this datagram.
     *
     * @return the list.
     */
    public List<RakEncapsulatedPacket> getPackets() {
        return packets;
    }

    @Override
    protected void deallocate() {
        packets.forEach(ReferenceCountUtil::release);
        packets.clear();
    }

    @Override
    public ReferenceCounted touch(Object hint) {
        return this;
    }
}
