package org.crimson.network2.raknet;

import com.google.common.base.Preconditions;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import org.crimson.network2.raknet.packet.RakNetPacket;
import org.crimson.network2.raknet.packet.client.ClientUnconnectedPing;
import org.crimson.network2.raknet.protocol.RakNetProtocol;
import org.crimson.network2.utility.BootstrapChannelConfiguration;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Represents an implementation of a RakNet server.
 */
public abstract class RakNet implements AutoCloseable {

    /**
     * The RakNet GUID.
     */
    protected final long guid = ThreadLocalRandom.current().nextLong();

    /**
     * The current running state of the server.
     */
    protected final AtomicBoolean running = new AtomicBoolean();

    /**
     * The server netty bootstrap.
     */
    protected final Bootstrap bootstrap = new Bootstrap();

    /**
     * The tick future.
     */
    protected ScheduledFuture<?> future;

    /**
     * Initialize the bootstrap here.
     *
     * @param channelConfiguration the common channel configuration
     */
    public RakNet(BootstrapChannelConfiguration.Channels channelConfiguration) {
        bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        bootstrap.channel(channelConfiguration.channel());
        bootstrap.group(channelConfiguration.group());
    }

    /**
     * Bind to the given address by whoever implements {@code bindInternally}
     *
     * @return a {@link CompletableFuture} containing the result.
     */
    public CompletableFuture<Void> bind() {
        Preconditions.checkState(running.compareAndSet(false, true), "RakNet server has already been started!");

        final CompletableFuture<Void> result = bindInternally();
        result.whenComplete((v, error) -> {
            if (error != null) {
                running.compareAndSet(true, false);
            } else {
                future = bootstrap.config().group().next().scheduleAtFixedRate(this::onTick, 0, 10, TimeUnit.MILLISECONDS);
            }
        });

        return result;
    }

    /**
     * @return the unique server ID.
     */
    public long getServerId() {
        return guid;
    }

    /**
     * Bind to the address internally and return when done.
     *
     * @return a {@link CompletableFuture} containing the result.
     */
    protected abstract CompletableFuture<Void> bindInternally();

    /**
     * Tick the server every 10 milliseconds.
     */
    protected abstract void onTick();

    /**
     * Handle incoming ping requests.
     *
     * @param context the context
     * @param sender  the sender
     * @param packet  the unconnected ping packet.
     */
    protected abstract void handleIncomingPing(ChannelHandlerContext context, InetSocketAddress sender, ClientUnconnectedPing packet);

    /**
     * Handle an incoming connection request.
     *
     * @param context the context
     * @param sender  the sender
     * @param content the byte buf
     */
    protected abstract void handleIncomingConnectionRequest(ChannelHandlerContext context, InetSocketAddress sender, ByteBuf content);

    /**
     * Send an incompatible protocol message to the {@code sender}
     *
     * @param context the context
     * @param sender  the sender
     */
    protected void sendIncompatibleProtocol(ChannelHandlerContext context, InetSocketAddress sender) {
        RakNetPacket.createPacket(context, RakNetProtocol.INCOMPATIBLE_PROTOCOL, 26)
                .writeId()
                .writeByte(RakNetProtocol.PROTOCOL_VERSION)
                .writeMagic()
                .writeLong(guid)
                .encodeAndWriteTo(context, sender);
    }

    /**
     * Send no free connections are available to the {@code sender}
     *
     * @param context the context
     * @param sender  the sender
     */
    protected void sendNoFreeIncomingConnections(ChannelHandlerContext context, InetSocketAddress sender) {
        RakNetPacket.createPacket(context, RakNetProtocol.NO_FREE_INCOMING_CONNECTIONS, 25)
                .writeId()
                .writeMagic()
                .writeLong(guid)
                .encodeAndWriteTo(context, sender);
    }

    /**
     * Send they are already connected to the {@code sender}
     *
     * @param context the context
     * @param sender  the sender
     */
    protected void sendAlreadyConnected(ChannelHandlerContext context, InetSocketAddress sender) {
        RakNetPacket.createPacket(context, RakNetProtocol.ALREADY_CONNECTED, 25)
                .writeId()
                .writeMagic()
                .writeLong(guid)
                .encodeAndWriteTo(context, sender);
    }

    @Override
    public void close() {
        future.cancel(true);
    }
}
