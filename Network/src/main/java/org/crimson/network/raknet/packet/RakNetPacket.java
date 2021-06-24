package org.crimson.network.raknet.packet;

import com.google.common.base.Charsets;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import org.crimson.network.raknet.protocol.RakNetProtocol;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetSocketAddress;

/**
 * Represents a RakNet packet.
 */
public abstract class RakNetPacket {

    /**
     * The buffer
     */
    protected ByteBuf buffer;

    /**
     * Sanity check.
     */
    protected boolean idWritten;

    /**
     * Initialize this packet.
     *
     * @param packet the packet
     */
    protected RakNetPacket(ByteBuf packet) {
        buffer = packet;
    }

    /**
     * @param allocate indicates if the buffer should be created.
     */
    protected RakNetPacket(boolean allocate) {
        if (allocate) buffer = Unpooled.buffer();
    }

    /**
     * Initialize this packet with a set a buffer
     *
     * @param context        the context
     * @param allocateAmount the allocate amount
     */
    protected RakNetPacket(ChannelHandlerContext context, int allocateAmount) {
        this.buffer = context.alloc().ioBuffer(allocateAmount, allocateAmount);
    }

    /**
     * Create a empty packet.
     *
     * @param context the context
     * @param pid     the pid
     * @param amount  the allocate amount
     * @return a new {@link RakNetPacket}
     */
    public static RakNetPacket createPacket(ChannelHandlerContext context, byte pid, int amount) {
        return new RakNetPacket(context, amount) {
            @Override
            public byte getId() {
                return pid;
            }

            @Override
            public void decode() {

            }

            @Override
            public void encode() {

            }
        };
    }

    /**
     * Create a empty packet.
     *
     * @param allocate the buffer.
     * @param pid      the pid
     * @return a new {@link RakNetPacket}
     */
    public static RakNetPacket createPacket(ByteBuf allocate, byte pid) {
        return new RakNetPacket(allocate) {
            @Override
            public byte getId() {
                return pid;
            }

            @Override
            public void decode() {

            }

            @Override
            public void encode() {

            }
        };
    }

    /**
     * Allocate the buffer
     *
     * @param context the context
     * @param amount  the amount
     */
    public RakNetPacket allocate(ChannelHandlerContext context, int amount) {
        buffer = context.alloc().ioBuffer(amount, amount);
        return this;
    }

    /**
     * Encode this packet and then write this packet to the channel.
     *
     * @param context   the context
     * @param recipient who its going to
     */
    public void encodeAndWriteTo(ChannelHandlerContext context, InetSocketAddress recipient) {
        encode();

        if (!idWritten) throw new IllegalStateException("Packet ID was not written!");
        context.writeAndFlush(new DatagramPacket(buffer, recipient), context.voidPromise());
    }


    /**
     * Encode this packet and then write this packet to the channel.
     *
     * @param context   the context
     * @param recipient who its going to
     */
    public void encodeAndWriteTo(Channel context, InetSocketAddress recipient) {
        if (!idWritten) throw new IllegalStateException("Packet ID was not written!");

        encode();
        context.writeAndFlush(new DatagramPacket(buffer, recipient), context.voidPromise());
    }

    /**
     * @return the packet ID.
     */
    public abstract byte getId();

    /**
     * Decode this packet.
     */
    public abstract void decode();

    /**
     * Encode this packet
     */
    public abstract void encode();

    /**
     * Verify the RakNet magic value.
     *
     * @return {@code true} if the magic is correct.
     */
    public boolean verifyMagic() {
        return true;
    }

    /**
     * Verify the protocol version.
     *
     * @return {@code true} if the protocol version is correct.
     */
    public boolean verifyProtocolVersion() {
        return true;
    }

    /**
     * Read a long value.
     *
     * @return the long
     */
    public long readLong() {
        return buffer.readLong();
    }

    /**
     * Read boolean
     *
     * @return the boolean
     */
    public boolean readBoolean() {
        return buffer.readBoolean();
    }

    /**
     * Read 16 bytes as a magic value.
     *
     * @return the bytes
     */
    public byte[] readMagic() {
        final var result = new byte[16];
        buffer.readBytes(result, 0, 16);
        return result;
    }

    /**
     * Read a byte
     *
     * @return the byte
     */
    public byte readByte() {
        return buffer.readByte();
    }

    /**
     * Read a byte
     *
     * @return the byte
     */
    public int readUnsignedByte() {
        return buffer.readUnsignedByte();
    }

    /**
     * Read X amount of bytes
     *
     * @param amount the amount
     * @return the bytes
     */
    public byte[] readBytes(int amount) {
        final var result = new byte[16];
        buffer.readBytes(result, 0, amount);
        return result;
    }

    /**
     * Read short
     *
     * @return the short
     */
    public short readShort() {
        return buffer.readShort();
    }

    /**
     * Write a long.
     *
     * @param value the value
     */
    public RakNetPacket writeLong(long value) {
        buffer.writeLong(value);
        return this;
    }

    /**
     * Write a short value
     *
     * @param value the value
     */
    public RakNetPacket writeShort(int value) {
        buffer.writeShort(value);
        return this;
    }

    /**
     * Write a IPv4 or IPv6 address.
     * https://github.com/NukkitX/Network/blob/79a77142ab3e34d9f45170dec1e6f906c9cf53ca/common/src/main/java/com/nukkitx/network/NetworkUtils.java#L40
     *
     * @param address the address.
     */
    public RakNetPacket writeAddress(InetSocketAddress address) {
        final var add = address.getAddress();
        final var bytes = address.getAddress().getAddress();
        if (add instanceof Inet4Address) {
            buffer.writeByte(4);
            for(int i = 0; i < bytes.length; i++) bytes[i] = (byte) (~bytes[i] & 0xFF);
            buffer.writeBytes(bytes);
            buffer.writeShort(address.getPort());
        } else if (add instanceof Inet6Address) {
            buffer.writeByte(6);
            buffer.writeShortLE(23);
            buffer.writeShort(address.getPort());
            buffer.writeInt(0);
            buffer.writeBytes(bytes);
            buffer.writeInt(((Inet6Address) add).getScopeId());
        }

        return this;
    }

    /**
     * Write the magic.
     */
    public RakNetPacket writeMagic() {
        buffer.writeBytes(RakNetProtocol.MAGIC);
        return this;
    }

    /**
     * Write a string.
     *
     * @param value the value
     */
    public RakNetPacket writeString(String value) {
        final var bytes = value.getBytes(Charsets.UTF_8);
        buffer.writeShort(bytes.length);
        buffer.writeBytes(bytes);

        return this;
    }

    /**
     * Write the packet ID.
     */
    public RakNetPacket writeId() {
        idWritten = true;
        buffer.writeByte(getId());
        return this;
    }

    /**
     * Write a byte.
     */
    public RakNetPacket writeByte() {
        buffer.writeByte(0);
        return this;
    }

    /**
     * Write a byte.
     *
     * @param value the value
     */
    public RakNetPacket writeByte(int value) {
        buffer.writeByte(value);
        return this;
    }

    /**
     * Write a boolean
     *
     * @param value the value
     */
    public RakNetPacket writeBoolean(boolean value) {
        buffer.writeBoolean(value);
        return this;
    }

    /**
     * https://github.com/CloudburstMC/Network/blob/607c81b9b4194d1bdadb2801eb795ca061081a4f/common/src/main/java/com/nukkitx/network/NetworkUtils.java#L12
     *
     * @return the address
     */
    protected InetSocketAddress readAddress() {
        final short type = buffer.readByte();

        try {
            if (type == 4) {
                final byte[] address = new byte[4];
                buffer.readBytes(address);

                for (int i = 0; i < address.length; i++) {
                    address[i] = (byte) (~address[i] & 0xFF);
                }

                return new InetSocketAddress(Inet4Address.getByAddress(address), buffer.readUnsignedShort());
            } else if (type == 6) {
                buffer.readShortLE();
                final int port = buffer.readUnsignedShort();
                buffer.readInt();
                final byte[] address = new byte[16];
                buffer.readBytes(address);
                return new InetSocketAddress(Inet6Address.getByAddress(null, address, buffer.readInt()), port);
            }
        } catch (Exception any) {
            throw new RuntimeException(any);
        }

        return null;
    }

    /**
     * @return the buffer.
     */
    public ByteBuf buffer() {
        return buffer;
    }
}
