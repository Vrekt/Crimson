package org.crimson.v3.raknet.offline;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import org.crimson.v3.RakNetServer;
import org.crimson.v3.netty.impl.RakNetChannel;
import org.crimson.v3.raknet.RakNetProtocol;

import java.net.InetSocketAddress;

/**
 * The default encoder for offline messages.
 */
public record DefaultRakOfflineEncoder(RakNetServer server) implements RakOfflineEncoder {

    @Override
    public void sendUnconnectedPong(ChannelHandlerContext context, InetSocketAddress recipient, long pingTime) {
        final ByteBuf buffer = context.alloc().ioBuffer(31);
        final byte[] ping = server.getEventHandler().onPing().getResponse();

        buffer.writeByte(RakNetProtocol.UNCONNECTED_PONG);
        buffer.writeLong(pingTime);
        buffer.writeLong(server.getId());
        buffer.writeBytes(RakNetProtocol.MAGIC);
        buffer.writeShort(ping.length);
        buffer.writeBytes(ping);

        context.writeAndFlush(new DatagramPacket(buffer, recipient));
    }

    @Override
    public void sendConnectionBanned(ChannelHandlerContext context, InetSocketAddress recipient) {
        final ByteBuf buffer = context.alloc().ioBuffer(25, 25);
        buffer.writeByte(RakNetProtocol.CONNECTION_BANNED);
        buffer.writeBytes(RakNetProtocol.MAGIC);
        buffer.writeLong(server.getId());

        context.writeAndFlush(new DatagramPacket(buffer, recipient));
    }

    @Override
    public void sendIncompatibleProtocol(ChannelHandlerContext context, InetSocketAddress recipient, int protocolVersion) {
        final ByteBuf buffer = context.alloc().ioBuffer(26, 26);
        buffer.writeByte(RakNetProtocol.INCOMPATIBLE_PROTOCOL);
        buffer.writeByte(protocolVersion);
        buffer.writeBytes(RakNetProtocol.MAGIC);
        buffer.writeLong(server.getId());

        context.writeAndFlush(new DatagramPacket(buffer, recipient));
    }

    @Override
    public void sendAlreadyConnected(ChannelHandlerContext context, InetSocketAddress recipient) {
        final ByteBuf buffer = context.alloc().ioBuffer(25, 25);
        buffer.writeByte(RakNetProtocol.ALREADY_CONNECTED);
        buffer.writeBytes(RakNetProtocol.MAGIC);
        buffer.writeLong(server.getId());

        context.writeAndFlush(new DatagramPacket(buffer, recipient));
    }

    @Override
    public void sendOpenConnectionReply1(ChannelHandlerContext context, InetSocketAddress recipient, int mtu) {
        final ByteBuf buffer = context.alloc().ioBuffer(28, 28);
        buffer.writeByte(RakNetProtocol.OPEN_CONNECTION_REPLY_1);
        buffer.writeBytes(RakNetProtocol.MAGIC);
        buffer.writeLong(server.getId());
        buffer.writeBoolean(false);
        buffer.writeShort(mtu);

        context.writeAndFlush(new DatagramPacket(buffer, recipient));
    }

    @Override
    public void sendOpenConnectionReply2(ChannelHandlerContext context, RakNetChannel channel, InetSocketAddress recipient, InetSocketAddress other, int mtu) {
        final ByteBuf buffer = context.alloc().ioBuffer(31);
        buffer.writeByte(RakNetProtocol.OPEN_CONNECTION_REPLY_2);
        buffer.writeBytes(RakNetProtocol.MAGIC);
        buffer.writeLong(server.getId());

        RakNetProtocol.writeAddress(other, buffer);
        buffer.writeShort(mtu);
        buffer.writeBoolean(false);

        channel.writeAndFlush(new DatagramPacket(buffer, recipient));
    }

}
