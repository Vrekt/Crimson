package org.crimson.v3.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import lombok.extern.log4j.Log4j2;
import org.crimson.v3.netty.pipeline.offline.RakOfflineHandler;
import org.crimson.v3.utility.LocalNetwork;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Represents the main server channel.
 * <p>
 * Based off this implementation:
 * https://github.com/Shevchik/UdpServerSocketChannel/blob/master/src/udpserversocketchannel/channel/UdpServerChannel.java
 */
@Log4j2
public abstract class RakServerChannel extends AbstractServerChannel {

    /**
     * Channel config.
     */
    private final ChannelConfig channelConfig = new DefaultChannelConfig(this)
            .setAutoRead(true)
            .setRecvByteBufAllocator(new FixedRecvByteBufAllocator(2048));

    /**
     * The group the channel is using.
     */
    protected final EventLoopGroup group;

    /**
     * Set of bootstraps that should be bound.
     */
    protected final List<Bootstrap> bootstraps = new ArrayList<>();

    /**
     * Set of channels that are bound.
     */
    protected final List<Channel> channels = new ArrayList<>();

    /**
     * Main write channel.
     */
    protected Channel mainChannel;

    /**
     * Offline message handler.
     */
    private RakOfflineHandler offlineHandler;

    /**
     * If this channel is open/active.
     */
    private final AtomicBoolean open = new AtomicBoolean();

    /**
     * Initializes the server channel.
     */
    public RakServerChannel() {
        group = LocalNetwork.getDefaultGroup();
        open.set(true);

        // default route initializer.
        final ChannelInitializer<Channel> defaultRouteInitializer = new ChannelInitializer<>() {
            @Override
            protected void initChannel(Channel channel) {
                channel.pipeline().addLast(new RakServerRouteHandler(RakServerChannel.this));
                channel.pipeline().addLast(offlineHandler);
            }
        };

        final Bootstrap bootstrap = new Bootstrap()
                .group(group)
                .channel(LocalNetwork.getDefaultChannel())
                .handler(defaultRouteInitializer);
        bootstraps.add(bootstrap);
    }

    /**
     * Set the offline handler
     *
     * @param offlineHandler the handler.
     */
    public void setOfflineHandler(RakOfflineHandler offlineHandler) {
        this.offlineHandler = offlineHandler;
    }

    /**
     * @return the offline handler.
     */
    public RakOfflineHandler getOfflineHandler() {
        return offlineHandler;
    }

    @Override
    protected boolean isCompatible(EventLoop loop) {
        return true;
    }

    @Override
    protected SocketAddress localAddress0() {
        return channels.get(0).localAddress();
    }

    @Override
    protected void doBind(SocketAddress localAddress) throws Exception {
        for (Bootstrap bootstrap : bootstraps) {
            channels.add(bootstrap.bind(localAddress).sync().channel());
        }
        mainChannel = channels.get(0);
        bootstraps.clear();
    }

    @Override
    protected void doBeginRead() throws Exception {

    }

    @Override
    protected void doClose() throws Exception {
        open.set(false);
        mainChannel.close();
        channels.clear();
        group.shutdownGracefully().sync();
    }

    @Override
    public ChannelConfig config() {
        return channelConfig;
    }

    @Override
    public boolean isOpen() {
        return open.get();
    }

    @Override
    public boolean isActive() {
        return open.get();
    }
}
