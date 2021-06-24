package org.crimson.network.raknet.packet.encapsulated;

import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCounted;
import org.crimson.network.raknet.protocol.RakNetProtocol;
import org.crimson.network.raknet.reliability.RakNetReliability;

/**
 * Represents an encapsulated RakNet packet.
 * <p>
 * Reference:
 * https://github.com/CloudburstMC/Network/blob/607c81b9b4194d1bdadb2801eb795ca061081a4f/raknet/src/main/java/com/nukkitx/network/raknet/EncapsulatedPacket.java
 */
public final class EncapsulatedPacket implements ReferenceCounted {

    /**
     * The reliability of this packet.
     */
    private RakNetReliability reliability;

    /**
     * If the packet should be split?
     */
    private boolean split;

    /**
     * Index's for reliability and sequence
     * The ordered frame index and channel.
     * TODO: short ?
     */
    private int reliabilityFrameIndex, sequenceFrameIndex, orderedFrameIndex, orderedChannel;

    /**
     * Compound/part information
     */
    private int compoundSize, compoundId, compoundIndex;

    /**
     * The slice of this packet.
     */
    private ByteBuf buffer;

    /**
     * Decode this encapsulated packet.
     *
     * @param content the content.
     */
    public void decode(ByteBuf content) {
        final byte flags = content.readByte();
        // ?
        reliability = RakNetReliability.get((flags & RakNetProtocol.RELIABILITY) >> 5);
        split = (flags & RakNetProtocol.SPLIT) != 0;

        final int size = (content.readUnsignedShort() + 7) >> 3;
        if (reliability.isReliable()) reliabilityFrameIndex = content.readUnsignedMediumLE();
        if (reliability.isSequenced()) sequenceFrameIndex = content.readUnsignedMediumLE();
        if (reliability.isOrdered() || reliability.isSequenced()) {
            orderedFrameIndex = content.readUnsignedMediumLE();
            orderedChannel = content.readUnsignedByte();
        }

        if (split) {
            compoundSize = content.readInt();
            compoundId = content.readShort();
            compoundIndex = content.readInt();
        }

        buffer = content.readSlice(size);
    }

    /**
     * @return {@code true} if this packet is split.
     */
    public boolean isSplit() {
        return split;
    }

    /**
     * @return the buffer
     */
    public ByteBuf getBuffer() {
        return buffer;
    }

    /**
     * @return the reliability of this packet.
     */
    public RakNetReliability getReliability() {
        return reliability;
    }

    @Override
    public int refCnt() {
        return 0;
    }

    @Override
    public ReferenceCounted retain() {
        return null;
    }

    @Override
    public ReferenceCounted retain(int increment) {
        return null;
    }

    @Override
    public ReferenceCounted touch() {
        return null;
    }

    @Override
    public ReferenceCounted touch(Object hint) {
        return null;
    }

    @Override
    public boolean release() {
        return false;
    }

    @Override
    public boolean release(int decrement) {
        return false;
    }
}
