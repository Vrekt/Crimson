package org.crimson.v3.raknet.offline;

import io.netty.channel.ChannelHandlerContext;
import org.crimson.v3.netty.impl.RakNetChannel;

import java.net.InetSocketAddress;

/**
 * Encoding interface for offline messages.
 */
public interface RakOfflineEncoder {


    /**
     * Send unconnected pong response.
     *
     * @param context   the context
     * @param recipient the recipient
     * @param pingTime  the ping time
     */
    void sendUnconnectedPong(ChannelHandlerContext context, InetSocketAddress recipient, long pingTime);

    /**
     * Send connection banned
     *
     * @param context   the context
     * @param recipient the recipient
     */
    void sendConnectionBanned(ChannelHandlerContext context, InetSocketAddress recipient);

    /**
     * Send incompatible protocol.
     *
     * @param context         the context
     * @param recipient       the recipient
     * @param protocolVersion the incompatible protocol version.
     */
    void sendIncompatibleProtocol(ChannelHandlerContext context, InetSocketAddress recipient, int protocolVersion);

    /**
     * Send already connected
     *
     * @param context   the context
     * @param recipient the recipient
     */
    void sendAlreadyConnected(ChannelHandlerContext context, InetSocketAddress recipient);

    /**
     * Send the first open connection reply.
     *
     * @param context   the context
     * @param recipient the recipient
     * @param mtu       the MTU size.
     */
    void sendOpenConnectionReply1(ChannelHandlerContext context, InetSocketAddress recipient, int mtu);

    /**
     * Send the second open connection reply.
     *
     * @param context   the context
     * @param channel   the channel
     * @param recipient the recipient
     * @param other     the address to write.
     * @param mtu       the MTU size.
     */
    void sendOpenConnectionReply2(ChannelHandlerContext context, RakNetChannel channel, InetSocketAddress recipient, InetSocketAddress other, int mtu);

}
