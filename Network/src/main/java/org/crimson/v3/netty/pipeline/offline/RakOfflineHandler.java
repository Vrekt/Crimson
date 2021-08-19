package org.crimson.v3.netty.pipeline.offline;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.log4j.Log4j2;
import org.crimson.v3.RakNetServer;
import org.crimson.v3.netty.RakServerChannel;
import org.crimson.v3.netty.impl.RakNetChannel;
import org.crimson.v3.netty.impl.RakNetServerChannel;
import org.crimson.v3.netty.pipeline.AcceptableInboundMessageHandler;
import org.crimson.v3.raknet.RakNetProtocol;
import org.crimson.v3.raknet.offline.DefaultRakOfflineEncoder;
import org.crimson.v3.raknet.offline.RakOfflineEncoder;
import org.crimson.v3.utility.LocalNetwork;

import java.net.Inet6Address;
import java.net.InetSocketAddress;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Handles offline messages.
 */
@Log4j2
@ChannelHandler.Sharable
public final class RakOfflineHandler extends AcceptableInboundMessageHandler<DatagramPacket> {

    /**
     * Holds a cache of offline connections attempting to connect.
     */
    private final Cache<InetSocketAddress, Integer> offlineConnections = CacheBuilder.newBuilder()
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .removalListener(notification -> ReferenceCountUtil.release(notification.getValue()))
            .build();

    /**
     * Map of banned ip addresses.
     * <p>
     * Useful for when you want them totally blocked, with no reason.
     */
    private final Set<InetSocketAddress> bannedIpAddresses = ConcurrentHashMap.newKeySet();

    /**
     * The encoder.
     */
    private final RakOfflineEncoder encoder;

    /**
     * Server channel.
     */
    private RakNetServerChannel rakNetServerChannel;

    public RakOfflineHandler(RakNetServer server) {
        super(DatagramPacket.class);

        encoder = new DefaultRakOfflineEncoder(server);
    }

    /**
     * Set rak server channel
     *
     * @param rakNetServerChannel the channel
     */
    public void setRakNetServerChannel(RakServerChannel rakNetServerChannel) {
        this.rakNetServerChannel = (RakNetServerChannel) rakNetServerChannel;
    }

    /**
     * Ban an ip-address.
     *
     * @param address the address
     */
    public void banIpAddress(InetSocketAddress address) {
        this.bannedIpAddresses.add(address);
    }

    @Override
    protected boolean acceptOrDeclineMessage0(ChannelHandlerContext context, DatagramPacket message) {
        final ByteBuf content = message.content();
        if (!content.isReadable()) return false;

        final int id = content.readUnsignedByte();
        log.info("ID: " + id);
        try {
            switch (id) {
                case RakNetProtocol.UNCONNECTED_PING:
                    if (content.isReadable(8)) content.readLong();
                case RakNetProtocol.OPEN_CONNECTION_REQUEST_1:
                case RakNetProtocol.OPEN_CONNECTION_REQUEST_2:
                    return content.isReadable(16) && RakNetProtocol.isMagic(content);
            }
        } finally {
            content.readerIndex(0);
        }

        return false;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext context, DatagramPacket message) {
        final ByteBuf content = message.content();
        final int id = content.readUnsignedByte();
        final InetSocketAddress sender = message.sender();

        switch (id) {
            case RakNetProtocol.UNCONNECTED_PING -> onIncomingPing(context, sender, content);
            case RakNetProtocol.OPEN_CONNECTION_REQUEST_1 -> onOpenConnectionRequest1(context, sender, content);
            case RakNetProtocol.OPEN_CONNECTION_REQUEST_2 -> onOpenConnectionRequest2(context, sender, message, content);
        }
    }

    /**
     * Handle incoming pings.
     *
     * @param context   the context
     * @param recipient the recipient
     * @param content   the content
     */
    private void onIncomingPing(ChannelHandlerContext context, InetSocketAddress recipient, ByteBuf content) {
        final long time = content.readLong();
        encoder.sendUnconnectedPong(context, recipient, time);
    }

    /**
     * Handle the first connection request.
     * <p>
     *
     * @param context   the context
     * @param recipient the recipient
     * @param content   the content
     */
    private void onOpenConnectionRequest1(ChannelHandlerContext context, InetSocketAddress recipient, ByteBuf content) {
        content.skipBytes(16);

        // ensure sender is not banned.
        if (bannedIpAddresses.contains(recipient)) {
            encoder.sendConnectionBanned(context, recipient);
            return;
        }

        // ensure sender is not already connected.
        if (offlineConnections.getIfPresent(recipient) != null) {
            encoder.sendAlreadyConnected(context, recipient);
            return;
        }

        // ensure we have a valid protocol.
        final int protocolVersion = content.readUnsignedByte();
        if (protocolVersion != RakNetProtocol.PROTOCOL_VERSION) {
            encoder.sendIncompatibleProtocol(context, recipient, protocolVersion);
            return;
        }

        // add this to a pending connections list now.
        offlineConnections.put(recipient, protocolVersion);

        // Credit: CloudburstMC Network 2.0-
        final int mtu = content.readableBytes() + 1 + 16 + 1 + (recipient.getAddress() instanceof Inet6Address ? 40 : 20) + 8;
        encoder.sendOpenConnectionReply1(context, recipient, RakNetProtocol.clampMtuSize(mtu));
    }

    /**
     * Handle the second connection request.
     *
     * @param context   the context
     * @param recipient the recipient
     * @param content   the content
     */
    private void onOpenConnectionRequest2(ChannelHandlerContext context, InetSocketAddress recipient, DatagramPacket p, ByteBuf content) {
        content.skipBytes(16);

        // ensure we already have a pending connection
        final Integer protocolVersion = offlineConnections.getIfPresent(recipient);
        if (protocolVersion == null) return;

        // retrieve server address the client wants to connect to.
        final InetSocketAddress address = LocalNetwork.readAddress(content);
        final int mtu = content.readUnsignedShort();
        final long guid = content.readLong();

        // initialize a new channel.
        final RakNetChannel channel = rakNetServerChannel.createRakChannel(recipient);
        if (channel == null) {
            encoder.sendAlreadyConnected(context, recipient);
            return;
        }

        // set properties
        channel.setProtocolVersion(protocolVersion);
        channel.setMtu(mtu);
        channel.setGuid(guid);

        // finally, send off.
        encoder.sendOpenConnectionReply2(context, channel, recipient, address, mtu);
    }

}
