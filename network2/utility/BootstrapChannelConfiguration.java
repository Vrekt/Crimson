package org.crimson.network2.utility;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.kqueue.KQueueDatagramChannel;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.util.concurrent.ThreadFactory;

/**
 * Provides a general utility class for retrieving the appropriate {@link io.netty.channel.EventLoopGroup} for the operating system.
 */
public final class BootstrapChannelConfiguration {

    /**
     * Represents the channel configuration for the current server.
     */
    static final Channels channelConfig;

    /**
     * Event thread factory
     */
    private static final ThreadFactory EVENT_THREAD_FACTORY = new ThreadFactoryBuilder()
            .setDaemon(true)
            .setNameFormat("Network Thread - #%d")
            .build();

    static {
        if (Epoll.isAvailable()) {
            channelConfig = Channels.EPOLL;
        } else if (KQueue.isAvailable()) {
            channelConfig = Channels.KQUEUE;
        } else {
            channelConfig = Channels.NIO;
        }
    }

    private BootstrapChannelConfiguration() {
        throw new UnsupportedOperationException();
    }

    /**
     * @return the {{@link Channels} configuration.
     */
    public static Channels common() {
        return BootstrapChannelConfiguration.channelConfig;
    }

    /**
     * Holds specific channel data.
     */
    public enum Channels {

        /**
         * NIO
         */
        NIO(NioDatagramChannel.class, (KQueue.isAvailable() || Epoll.isAvailable()) ? null : new NioEventLoopGroup(EVENT_THREAD_FACTORY)),
        /**
         * K-QUEUE
         */
        KQUEUE(KQueueDatagramChannel.class, KQueue.isAvailable() ? new KQueueEventLoopGroup(EVENT_THREAD_FACTORY) : null),
        /**
         * E-POLL
         */
        EPOLL(EpollDatagramChannel.class, Epoll.isAvailable() ? new EpollEventLoopGroup(EVENT_THREAD_FACTORY) : null);

        /**
         * The channel
         */
        private final Class<? extends DatagramChannel> channel;
        /**
         * The group
         */
        private final EventLoopGroup group;

        Channels(Class<? extends DatagramChannel> channelClass, EventLoopGroup group) {
            this.channel = channelClass;
            this.group = group;
        }

        /**
         * @return the datagram channel
         */
        public Class<? extends DatagramChannel> channel() {
            return channel;
        }

        /**
         * @return the group
         */
        public EventLoopGroup group() {
            return group;
        }
    }

}
