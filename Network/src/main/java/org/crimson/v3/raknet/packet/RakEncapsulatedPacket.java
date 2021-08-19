package org.crimson.v3.raknet.packet;

import io.netty.buffer.ByteBuf;
import io.netty.util.AbstractReferenceCounted;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.ReferenceCounted;
import lombok.extern.log4j.Log4j2;
import org.crimson.v3.raknet.RakNetProtocol;
import org.crimson.v3.raknet.reliability.RakNetReliability;

/**
 * Represents a packet that was encapsulated inside a {@link RakNetDatagram}
 */
@Log4j2
public final class RakEncapsulatedPacket extends AbstractReferenceCounted {

    /**
     * Contents of this packet.
     */
    private ByteBuf contents;

    /**
     * The reliability of this packet.
     */
    private RakNetReliability reliability;

    /**
     * The reliability index
     */
    private int reliabilityIndex;

    /**
     * Try to decode this packet.
     * TODO: Handle disconnect.
     *
     * @param content the content
     * @return the result of the decode.
     */
    boolean decode(ByteBuf content) {
        try {
            final byte flag = content.readByte();
            reliability = RakNetReliability.of((flag & RakNetProtocol.RELIABILITY) >> 5);
            final boolean isSplit = (flag & RakNetProtocol.SPLIT) != 0;

            final int length = (content.readUnsignedShort() + 7) >> 3;
            // read the index if this packet is reliable.
            if (reliability.isReliable()) reliabilityIndex = content.readUnsignedMediumLE();
            this.contents = content.readRetainedSlice(length);
            return true;
        } catch (Exception any) {
            any.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieve the contents buffer.
     *
     * @return the buffer.
     */
    public ByteBuf getContents() {
        return contents;
    }

    @Override
    public RakEncapsulatedPacket retain() {
        return ReferenceCountUtil.retain(this);
    }

    @Override
    protected void deallocate() {
        ReferenceCountUtil.release(contents);
    }

    @Override
    public ReferenceCounted touch(Object hint) {
        return this;
    }
}
