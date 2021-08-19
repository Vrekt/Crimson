package org.crimson.v3.raknet.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.log4j.Log4j2;
import org.crimson.v3.raknet.RakNetProtocol;
import org.crimson.v3.raknet.packet.RakNetMessage;

import java.net.InetSocketAddress;

/**
 * Handle initial online connections coming in.
 */
@Log4j2
@ChannelHandler.Sharable
public final class RakInitialOnlineHandler extends SimpleChannelInboundHandler<RakNetMessage> {

    public static final RakInitialOnlineHandler INSTANCE = new RakInitialOnlineHandler();

    private RakInitialOnlineHandler() {
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RakNetMessage msg) {
        switch (msg.id()) {
            case RakNetProtocol.CONNECTION_REQUEST -> log.info("CONNECTION REQUEST.");
        }
    }

    private void onConnectionRequest(ChannelHandlerContext ctx, RakNetMessage message) {
        message.buffer().readLong(); // read client guid.
        final long time = message.buffer().readLong();

        final ByteBuf buffer = ctx.alloc().ioBuffer();
        buffer.writeByte(RakNetProtocol.CONNECTION_REQUEST_ACCEPTED);

        RakNetProtocol.writeAddress((InetSocketAddress) ctx.channel().remoteAddress(), buffer);
        buffer.writeShort(0);
        for (InetSocketAddress address : RakNetProtocol.LOCAL_IP_ADDRESSES_V4) {
            RakNetProtocol.writeAddress(address, buffer);
        }

        buffer.writeLong(time);
        buffer.writeLong(System.currentTimeMillis());
    }

}
