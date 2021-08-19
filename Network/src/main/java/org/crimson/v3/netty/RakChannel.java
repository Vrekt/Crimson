package org.crimson.v3.netty;

import io.netty.channel.*;
import lombok.extern.log4j.Log4j2;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Represents a RakNet UDP channel session.
 */
@Log4j2
public abstract class RakChannel extends AbstractChannel {

    /**
     * Channel config and meta.
     */
    private final ChannelMetadata channelMeta = new ChannelMetadata(false);
    private final ChannelConfig channelConfig = new DefaultChannelConfig(this);

    /**
     * If this channel is active and connected.
     */
    protected final AtomicBoolean connected = new AtomicBoolean(true);

    /**
     * The parent server.
     */
    protected final RakServerChannel serverChannel;

    /**
     * The remote address for this channel.
     */
    private final InetSocketAddress address;

    /**
     * If we are currently reading.
     */
    private boolean reading;

    public RakChannel(RakServerChannel channel, InetSocketAddress address) {
        super(channel.mainChannel);

        this.serverChannel = channel;
        this.address = address;
    }

    @Override
    protected AbstractUnsafe newUnsafe() {
        return new AbstractUnsafe() {
            @Override
            public void connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
                throw new UnsupportedOperationException("Cannot connect a RakChannel.");
            }
        };
    }

    @Override
    protected boolean isCompatible(EventLoop loop) {
        return true;
    }

    @Override
    protected SocketAddress localAddress0() {
        return serverChannel.localAddress0();
    }

    @Override
    protected SocketAddress remoteAddress0() {
        return address;
    }

    @Override
    protected void doBind(SocketAddress localAddress) {
        throw new UnsupportedOperationException("Cannot bind a RakChannel.");
    }

    @Override
    protected void doDisconnect() throws Exception {
        doClose();
    }

    @Override
    protected void doBeginRead() {
        // Ignored.
    }

    @Override
    protected void doWrite(ChannelOutboundBuffer in) {
        parent().writeAndFlush(in.current());
    }

    @Override
    public ChannelConfig config() {
        return channelConfig;
    }

    @Override
    public boolean isOpen() {
        return connected.get();
    }

    @Override
    public boolean isActive() {
        return connected.get();
    }

    @Override
    public ChannelMetadata metadata() {
        return channelMeta;
    }
}
