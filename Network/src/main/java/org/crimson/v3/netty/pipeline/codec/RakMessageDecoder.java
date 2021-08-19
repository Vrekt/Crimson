package org.crimson.v3.netty.pipeline.codec;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.crimson.v3.raknet.packet.RakEncapsulatedPacket;
import org.crimson.v3.raknet.packet.RakNetDatagram;
import org.crimson.v3.raknet.packet.RakNetMessage;

import java.util.List;

/**
 * Basic decoder that takes each packet within a {@link RakNetDatagram} and spits out each {@link RakEncapsulatedPacket}
 */
@ChannelHandler.Sharable
public final class RakMessageDecoder extends MessageToMessageDecoder<RakNetDatagram> {

    public static final RakMessageDecoder INSTANCE = new RakMessageDecoder();

    private RakMessageDecoder() {
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, RakNetDatagram msg, List<Object> out) {

        msg.getPackets().forEach(packet -> {
            final int pid = packet.getContents().readUnsignedByte();
            final RakNetMessage message = new RakNetMessage(pid, packet.getContents());
            out.add(message);
        });

    }
}
