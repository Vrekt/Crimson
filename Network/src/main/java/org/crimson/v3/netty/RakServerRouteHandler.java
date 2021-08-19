package org.crimson.v3.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import org.crimson.v3.netty.impl.RakNetChannel;
import org.crimson.v3.netty.impl.RakNetServerChannel;

/**
 * Basic routing handler.
 */
@ChannelHandler.Sharable
public final class RakServerRouteHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    /**
     * The parent server channel.
     */
    private final RakNetServerChannel serverChannel;

    public RakServerRouteHandler(RakServerChannel serverChannel) {
        this.serverChannel = (RakNetServerChannel) serverChannel;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) {
        final RakNetChannel channel = serverChannel.getChannel(msg.sender());
        if (channel == null) {
            // whoever is sending packets is not established yet.
            ctx.fireChannelRead(msg.retain());
            return;
        }

        // retrieve buffer, and pass it off.
        final ByteBuf buffer = msg.content().retain();

        if (channel.eventLoop().inEventLoop()) {
            channel.pipeline().fireChannelRead(buffer).fireChannelReadComplete();
        } else {
            channel.eventLoop().execute(() -> channel.pipeline().fireChannelRead(buffer).fireChannelReadComplete());
        }
    }
}
