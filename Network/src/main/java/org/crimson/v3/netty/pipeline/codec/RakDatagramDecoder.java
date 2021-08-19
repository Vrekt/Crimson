package org.crimson.v3.netty.pipeline.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.crimson.v3.raknet.RakNetProtocol;
import org.crimson.v3.raknet.packet.RakNetDatagram;

import java.util.List;

/**
 * Decodes incoming {@link DatagramPacket}'s to a new {@link RakNetDatagram}
 */
@ChannelHandler.Sharable
public final class RakDatagramDecoder extends MessageToMessageDecoder<ByteBuf> {

    public static final RakDatagramDecoder INSTANCE = new RakDatagramDecoder();

    private RakDatagramDecoder() {}

    @Override
    public boolean acceptInboundMessage(Object msg) throws Exception {
        final boolean inbound = super.acceptInboundMessage(msg);
        if (inbound) {
            // initially, check if we have a RakNet datagram.
            final ByteBuf content = (ByteBuf) msg;
            final byte potential = content.readByte();
            content.readerIndex(0);

            return RakNetProtocol.isRakNet(potential);
        } else {
            return false;
        }
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf content, List<Object> out) {
        if (!content.isReadable()) return;
        content.readByte(); // ignore initial potential byte.

        // initialize a new datagram and decode it.
        final RakNetDatagram datagram = new RakNetDatagram();

        if (datagram.decode(content)) {
            out.add(datagram);
        } else {
            ctx.channel().close();
        }
    }

}
