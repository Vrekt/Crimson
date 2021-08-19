package org.crimson.network2.raknet.packet.encapsulated;

import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCounted;
import lombok.extern.log4j.Log4j2;
import org.crimson.network2.raknet.protocol.RakNetProtocol;
import org.crimson.network2.raknet.reliability.RakNetReliability;

/**
 * Represents an encapsulated RakNet packet.
 * <p>
 * Reference:
 * https://github.com/CloudburstMC/Network/blob/607c81b9b4194d1bdadb2801eb795ca061081a4f/raknet/src/main/java/com/nukkitx/network/raknet/EncapsulatedPacket.java
 */
@Log4j2
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
     * Creates (decodes) a new {@link EncapsulatedPacket} from the provided {@code content}
     *
     * @param content the content
     * @return this
     */
    public static EncapsulatedPacket from(ByteBuf content) {
        final EncapsulatedPacket packet = new EncapsulatedPacket();
        packet.decode(content);
        return packet;
    }

    /**
     * Creates (encodes) a new {@link EncapsulatedPacket} from the provided {@code content}
     *
     * @param content the content
     * @return this
     */
    public static EncapsulatedPacket of(ByteBuf content) {
        final EncapsulatedPacket packet = new EncapsulatedPacket();
        packet.encode(content);
        return packet;
    }

    /**
     * @return a new instance of {@link EncapsulatedPacketBuilder}
     */
    public static EncapsulatedPacketBuilder builder() {
        return new EncapsulatedPacketBuilder();
    }

    /**
     * Encode this packet
     *
     * @param to the out buffer
     */
    public void encode(ByteBuf to) {
        int flags = reliability.ordinal() << 5;
        if (split) {
            flags |= 0b00010000;
        }

        to.writeByte(flags);
        to.writeShort(buffer.readableBytes() << 3);
        if (reliability.isReliable()) {
            to.writeMediumLE(reliabilityFrameIndex);
        }

        if (reliability.isSequenced()) {
            to.writeMediumLE(sequenceFrameIndex);
        }

        if (reliability.isOrdered() || reliability.isSequenced()) {
            to.writeMediumLE(orderedFrameIndex);
            to.writeByte(orderedChannel);
        }

        if (split) {
            to.writeInt(compoundSize);
            to.writeShort(compoundId);
            to.writeInt(compoundIndex);
        }

        to.writeBytes(buffer, buffer.readerIndex(), buffer.readableBytes());
        log.info("Encoded");
    }

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

    /**
     * @return the size of this packet.
     */
    public int getSize() {
        return 3 + reliability.getSize() + (split ? 10 : 0) + buffer.readableBytes();
    }

    @Override
    public int refCnt() {
        return buffer.refCnt();
    }

    @Override
    public ReferenceCounted retain() {
        return this.buffer.retain();
    }

    @Override
    public ReferenceCounted retain(int increment) {
        return this.buffer.retain(increment);
    }

    @Override
    public ReferenceCounted touch() {
        return this.buffer.touch();
    }

    @Override
    public ReferenceCounted touch(Object hint) {
        return this.buffer.touch(hint);
    }

    @Override
    public boolean release() {
        return this.buffer.release();
    }

    @Override
    public boolean release(int decrement) {
        return this.buffer.release(decrement);
    }

    /**
     * Builds new instances of {@link EncapsulatedPacket}
     */
    public static final class EncapsulatedPacketBuilder {

        /**
         * The packet.
         */
        private final EncapsulatedPacket packet = new EncapsulatedPacket();

        public EncapsulatedPacketBuilder setReliability(RakNetReliability reliability) {
            packet.reliability = reliability;
            return this;
        }

        public EncapsulatedPacketBuilder setSplit(boolean split) {
            packet.split = split;
            return this;
        }

        public EncapsulatedPacketBuilder setReliabilityFrameIndex(int reliabilityFrameIndex) {
            packet.reliabilityFrameIndex = reliabilityFrameIndex;
            return this;
        }

        public EncapsulatedPacketBuilder setSequenceFrameIndex(int sequenceFrameIndex) {
            packet.sequenceFrameIndex = sequenceFrameIndex;
            return this;
        }

        public EncapsulatedPacketBuilder setOrderedFrameIndex(int orderedFrameIndex) {
            packet.orderedFrameIndex = orderedFrameIndex;
            return this;
        }

        public EncapsulatedPacketBuilder setOrderedChannel(int orderedChannel) {
            packet.orderedChannel = orderedChannel;
            return this;
        }

        public EncapsulatedPacketBuilder setCompoundSize(int compoundSize) {
            packet.compoundSize = compoundSize;
            return this;
        }

        public EncapsulatedPacketBuilder setCompoundId(int compoundId) {
            packet.compoundId = compoundId;
            return this;
        }

        public EncapsulatedPacketBuilder setCompoundIndex(int compoundIndex) {
            packet.compoundIndex = compoundIndex;
            return this;
        }

        public EncapsulatedPacketBuilder setBuffer(ByteBuf buffer) {
            packet.buffer = buffer;
            return this;
        }

        /**
         * @return the internal {@link EncapsulatedPacket}
         */
        public EncapsulatedPacket build() {
            return packet;
        }

    }

}
