package org.crimson.v3.netty.impl;

import lombok.extern.log4j.Log4j2;
import org.crimson.v3.netty.RakServerChannel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Default implementation of {@link org.crimson.v3.netty.RakServerChannel}
 */
@Log4j2
public final class RakNetServerChannel extends RakServerChannel {

    /**
     * Map of channels
     */
    private final ConcurrentMap<SocketAddress, RakNetChannel> channels = new ConcurrentHashMap<>();

    /**
     * Initializes the server channel.
     */
    public RakNetServerChannel() throws IOException {
        super();
    }

    @Override
    protected void doClose() throws Exception {
        super.doClose();

        channels.clear();
    }

    /**
     * Create a new {@link RakNetChannel} and initialize it.
     *
     * @param address the address
     * @return the new {@link RakNetChannel} or {@code null} if already created.
     */
    public RakNetChannel createRakChannel(InetSocketAddress address) {
        if (channels.containsKey(address)) return null;

        final RakNetChannel channel = new RakNetChannel(this, address);

        channel.closeFuture().addListener(future -> this.channels.remove(address));
        this.pipeline().fireChannelRead(channel).fireChannelReadComplete();

        this.channels.put(address, channel);
        return channel;
    }


    /**
     * Get a channel that corresponds with the address.
     *
     * @param address the address
     * @return the address or {@code null} if none
     */
    public RakNetChannel getChannel(InetSocketAddress address) {
        return channels.get(address);
    }

}
