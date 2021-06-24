package org.crimson.network.raknet.session;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.EventLoop;
import org.crimson.network.raknet.RakNetServer;
import org.crimson.network.raknet.packet.RakNetPacket;
import org.crimson.network.raknet.packet.datagram.RakNetDatagram;
import org.crimson.network.raknet.protocol.RakNetProtocol;
import org.crimson.network.raknet.session.state.RakNetConnectionState;

import java.net.InetSocketAddress;

/**
 * Represents a session within the {@link org.crimson.network.raknet.RakNetServer}
 */
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
     */
    protected int mtu;

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

        switch (pid) {
            case RakNetProtocol.OPEN_CONNECTION_REQUEST_2 -> handleSecondConnectionRequest(content);
            case RakNetProtocol.CONNECTION_REQUEST -> handleConnectionRequest(content);
        }
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
