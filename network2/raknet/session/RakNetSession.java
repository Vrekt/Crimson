package org.crimson.network2.raknet.session;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.EventLoop;
import io.netty.channel.socket.DatagramPacket;
import lombok.extern.log4j.Log4j2;
import org.crimson.network2.raknet.RakNetServer;
import org.crimson.network2.raknet.packet.RakNetPacket;
import org.crimson.network2.raknet.packet.datagram.RakNetDatagram;
import org.crimson.network2.raknet.packet.encapsulated.EncapsulatedPacket;
import org.crimson.network2.raknet.priority.RakNetPriority;
import org.crimson.network2.raknet.protocol.RakNetProtocol;
import org.crimson.network2.raknet.reliability.RakNetReliability;
import org.crimson.network2.raknet.session.state.RakNetConnectionState;

import java.net.Inet6Address;
import java.net.InetSocketAddress;

/**
 * Represents a session within the {@link RakNetServer}
 */
@Log4j2
public abstract class RakNetSession {

    /**
     * The server.
     */
    protected final RakNetServer server;

    /**
     * The session channel.
     */
    protected final Channel channel;

    /**
     * The address of this session
     */
    protected final InetSocketAddress address;

    /**
     * The event loop
     */
    private final EventLoop eventLoop;

    /**
     * The MTU size.
     * The adjusted MTU size accounting for header space.
     * The amount of datagrams sent.
     */
    protected int mtu, adjustedMtu, datagramWriteIndex;

    /**
     * The current connection state.
     */
    protected RakNetConnectionState state;

    /**
     * The last activity time.
     * Client GUID
     */
    protected long lastActivity, guid;

    /**
     * If this session is closed.
     */
    protected boolean isClosed;

    /**
     * Amount of times a packet is reliable.
     */
    private int reliableCount;

    /**
     * Initialize this session
     *
     * @param server  the server
     * @param address the address
     * @param channel the channel
     * @param mtu     the mtu
     */
    public RakNetSession(RakNetServer server, InetSocketAddress address, Channel channel, int mtu) {
        this.server = server;
        this.address = address;
        this.channel = channel;
        this.mtu = mtu;

        this.adjustedMtu = mtu - RakNetProtocol.UDP_HEADER_SIZE - (address.getAddress() instanceof Inet6Address ? 40 : 20);

        this.eventLoop = channel.eventLoop().next();
        this.state = RakNetConnectionState.INITIALIZING;
    }

    /**
     * Send the first open connection reply.
     */
    public abstract void sendOpenConnectionReply1();

    /**
     * Send the second open connection reply.
     */
    protected abstract void sendOpenConnectionReply2();

    /**
     * Send the connection request was accepted.
     *
     * @param time the client time
     */
    protected abstract void sendConnectionRequestAccepted(long time);

    /**
     * Handle the second connection request.
     *
     * @param content the content
     */
    protected abstract void handleSecondConnectionRequest(ByteBuf content);

    /**
     * Handle a connection request
     *
     * @param content the content
     */
    protected abstract void handleConnectionRequest(ByteBuf content);

    /**
     * Handles incoming datagram packets.
     *
     * @param content the content
     */
    public abstract void handleDatagram(ByteBuf content);

    /**
     * Handle a RakNet datagram
     *
     * @param datagram the datagram
     * @param content  the content
     */
    protected abstract void handleRakNetDatagram(RakNetDatagram datagram, ByteBuf content);

    /**
     * Close the session
     */
    protected abstract void close();

    /**
     * Handle a packet.
     *
     * @param content the content
     */
    protected void handlePacket(ByteBuf content) {
        content.readerIndex(0);
        final short pid = content.readUnsignedByte();

        log.info("Pid {}", pid);

        switch (pid) {
            case RakNetProtocol.OPEN_CONNECTION_REQUEST_2 -> handleSecondConnectionRequest(content);
            case RakNetProtocol.CONNECTION_REQUEST -> handleConnectionRequest(content);
        }
    }

    /**
     * Send a packet.
     *
     * @param content     the content
     * @param priority    the priority
     * @param reliability the reliability
     */
    protected void send(ByteBuf content, RakNetPriority priority, RakNetReliability reliability) {
        if (isClosed) return;

        if (eventLoop.inEventLoop()) {
            sendInternal(content, priority, reliability);
        } else {
            eventLoop.execute(() -> sendInternal(content, priority, reliability));
        }
    }

    /**
     * Send a packet.
     *
     * @param content     the content
     * @param priority    the priority
     * @param reliability the reliability
     */
    private void sendInternal(ByteBuf content, RakNetPriority priority, RakNetReliability reliability) {
        log.info("Sending content, reliability is: {}", reliability);

        try {
            final EncapsulatedPacket[] packets = createEncapsulatedPackets(content, reliability, 0);
            if (priority == RakNetPriority.IMMEDIATE) {
                sendNow(packets);
                return;
            }

            // TODO

        } finally {
            //  content.release();
        }

    }

    /**
     * Send the provided {@code packets} now.
     *
     * @param packets the packets
     */
    private void sendNow(EncapsulatedPacket[] packets) {
        for (EncapsulatedPacket packet : packets) {
            final RakNetDatagram datagram = RakNetDatagram.ofEncapsulated(packet);
            sendRakNetDatagram(datagram);
        }

        channel.flush();
    }

    /**
     * Send the provided {@code datagram}
     *
     * @param datagram the datagram
     */
    private void sendRakNetDatagram(RakNetDatagram datagram) {
        try {
            datagram.setSequence(datagramWriteIndex++);

            final ByteBuf buf = channel.alloc().ioBuffer(datagram.getSize());
            datagram.encodePacket(buf);
            channel.writeAndFlush(new DatagramPacket(buf, address));
        } finally {
            datagram.release();
        }
    }

    /**
     * Create an array of {@link EncapsulatedPacket}s
     * Only multiple packets are returned if the content exceeds the {@code mtu} size.
     *
     * @param content         the content
     * @param reliability     the reliability
     * @param orderingChannel the ordering channel
     * @return an array of packets.
     */
    protected EncapsulatedPacket[] createEncapsulatedPackets(ByteBuf content, RakNetReliability reliability, int orderingChannel) {
        final int maximumSize = adjustedMtu - RakNetProtocol.DATAGRAM_HEADER_SIZE - RakNetProtocol.MAXIMUM_ENCAPSULATED_HEADER_SIZE;

        // TODO: Packet splitting
        final EncapsulatedPacket[] packets = new EncapsulatedPacket[1];
        for (int i = 0; i < packets.length; i++) {
            packets[i] = EncapsulatedPacket.builder()
                    .setBuffer(content)
                    .setOrderedChannel(orderingChannel)
                    .setOrderedFrameIndex(0)
                    .setReliability(reliability)
                    .setReliabilityFrameIndex(reliability.isReliable() ? reliableCount++ : 0)
                    .build();
        }

        return packets;
    }

    /**
     * Disconnect this session.
     */
    public void disconnect() {
        if (isClosed) return;

        RakNetPacket.createPacket(channel.alloc().ioBuffer(1, 1), RakNetProtocol.DISCONNECTED)
                .writeId()
                .encodeAndWriteTo(channel, address);

        channel.close().syncUninterruptibly();
        isClosed = true;

        close();
    }

    /**
     * Send connection failed to the request.
     */
    protected void sendConnectionFailed() {
        RakNetPacket.createPacket(channel.alloc().ioBuffer(21), RakNetProtocol.CONNECTION_REQUEST_FAILED)
                .writeId()
                .writeMagic()
                .writeLong(server.getServerId())
                .encodeAndWriteTo(channel, address);
    }

    /**
     * Tick this session
     *
     * @param currentTime the current time in milliseconds.
     */
    public void tickSession(long currentTime) {
        eventLoop.execute(() -> tickInternal(currentTime));
    }

    /**
     * Tick internally.
     *
     * @param currentTime the current time
     */
    private void tickInternal(long currentTime) {
        if (isClosed) return;

        if (isTimedOut(currentTime)) {
            disconnect();
            return;
        }
    }

    /**
     * Updates this sessions last activity time.
     */
    protected void update() {
        lastActivity = System.currentTimeMillis();
    }

    /**
     * Check if this session has timed out.
     * TODO: Magic value
     *
     * @param currentTime the current time
     * @return {@code true} if so
     */
    private boolean isTimedOut(long currentTime) {
        return currentTime - lastActivity >= 10000;
    }

    /**
     * @return the connection state
     */
    public RakNetConnectionState getState() {
        return state;
    }

    /**
     * @return the session address
     */
    public InetSocketAddress getAddress() {
        return address;
    }

    /**
     * @return the event loop
     */
    public EventLoop getEventLoop() {
        return eventLoop;
    }
}
