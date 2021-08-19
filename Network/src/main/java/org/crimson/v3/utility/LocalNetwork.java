package org.crimson.v3.utility;

import io.netty.buffer.ByteBuf;
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
import org.crimson.v3.RakServer;

import java.net.*;

/**
 * Local network utility.
 */
public final class LocalNetwork {

    /**
     * Retrieve the OS/System default {@link EventLoopGroup}
     *
     * @return the group
     */
    public static EventLoopGroup getDefaultGroup() {
        return Epoll.isAvailable() ? new EpollEventLoopGroup(RakServer.EVENT_THREAD_FACTORY)
                : KQueue.isAvailable() ? new KQueueEventLoopGroup(RakServer.EVENT_THREAD_FACTORY)
                : new NioEventLoopGroup(RakServer.EVENT_THREAD_FACTORY);
    }

    /**
     * Retrieve the OS/System default {@link DatagramChannel}
     *
     * @return the channel
     */
    public static Class<? extends DatagramChannel> getDefaultChannel() {
        return Epoll.isAvailable() ? EpollDatagramChannel.class
                : KQueue.isAvailable() ? KQueueDatagramChannel.class
                : NioDatagramChannel.class;
    }

    /**
     * Credit: CloudburstMS Network 2.0-
     *
     * @param buffer --
     * @return --
     */
    public static InetSocketAddress readAddress(ByteBuf buffer) {
        short type = buffer.readByte();
        InetAddress address;
        int port;
        try {
            if (type == 4) {
                byte[] addressBytes = new byte[4];
                buffer.readBytes(addressBytes);

                for (int i = 0; i < addressBytes.length; i++) {
                    addressBytes[i] = (byte) (~addressBytes[i] & 0xFF);
                }

                address = Inet4Address.getByAddress(addressBytes);
                port = buffer.readUnsignedShort();
            } else if (type == 6) {
                buffer.readShortLE(); // Family, AF_INET6
                port = buffer.readUnsignedShort();
                buffer.readInt(); // Flow information
                byte[] addressBytes = new byte[16];
                buffer.readBytes(addressBytes);
                int scopeId = buffer.readInt();
                address = Inet6Address.getByAddress(null, addressBytes, scopeId);
            } else {
                throw new UnsupportedOperationException("Unknown Internet Protocol version.");
            }
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException(e);
        }
        return new InetSocketAddress(address, port);
    }

}
