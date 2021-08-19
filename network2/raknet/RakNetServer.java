package org.crimson.network2.raknet;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.ResourceLeakDetector;
import lombok.extern.log4j.Log4j2;
import org.crimson.network2.raknet.packet.RakNetPacket;
import org.crimson.network2.raknet.packet.RakNetPacketFactory;
import org.crimson.network2.raknet.packet.client.ClientConnectionRequest;
import org.crimson.network2.raknet.packet.client.ClientOpenConnectionRequest1;
import org.crimson.network2.raknet.packet.client.ClientOpenConnectionRequest2;
import org.crimson.network2.raknet.packet.client.ClientUnconnectedPing;
import org.crimson.network2.raknet.packet.server.ServerUnconnectedPong;
import org.crimson.network2.raknet.protocol.RakNetProtocol;
import org.crimson.network2.raknet.session.RakNetServerSession;
import org.crimson.network2.raknet.session.RakNetSession;
import org.crimson.network2.raknet.session.state.RakNetConnectionState;
import org.crimson.network2.utility.BedrockServerPing;
import org.crimson.network2.utility.BootstrapChannelConfiguration;
import org.crimson.v2.handler.ServerEventHandler;

import java.net.Inet6Address;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * The RakNetServer, an implementation of {@link RakNet}
 * <p>
 * Handles all incoming new connections and datagram packets.
 * <p>
 * TODO: Blocked addresses
 */
@Log4j2
public final class RakNetServer extends RakNet {

    /**
     * TODO: configuration
     */
    private static final int MAX_CONNECTION_LIMIT = 1024;

    /**
     * A pool of registered RakNet packets.
     */
    private final Map<Byte, RakNetPacketFactory<RakNetPacket>> pool = Map.of(
            RakNetProtocol.UNCONNECTED_PING, ClientUnconnectedPing::new,
            RakNetProtocol.OPEN_CONNECTION_REQUEST_1, ClientOpenConnectionRequest1::new,
            RakNetProtocol.OPEN_CONNECTION_REQUEST_2, ClientOpenConnectionRequest2::new,
            RakNetProtocol.CONNECTION_REQUEST, ClientConnectionRequest::new
    );

    /**
     * A map of sessions.
     */
    private final ConcurrentMap<InetSocketAddress, RakNetServerSession> sessions = new ConcurrentHashMap<>();

    /**
     * The datagram handler.
     */
    private final ServerDatagramHandler handler = new ServerDatagramHandler();

    /**
     * The server event handler.
     */
    private ServerEventHandler eventHandler;

    /**
     * The bind to address
     */
    private final InetSocketAddress bindTo;

    /**
     * Initialize the server.
     *
     * @param bindTo the bind to address
     */
    public RakNetServer(InetSocketAddress bindTo) {
        super(BootstrapChannelConfiguration.common());
        this.bindTo = bindTo;
    }

    /**
     * Set event handler
     *
     * @param eventHandler the handler
     */
    public void setEventHandler(ServerEventHandler eventHandler) {
        this.eventHandler = eventHandler;
        eventHandler.onInitialized(guid);
    }

    /**
     * Get a packet.
     *
     * @param pid     the PID
     * @param content the content
     * @param <T>     the RakNetPacket type.
     * @return the packet.
     */
    @SuppressWarnings("unchecked")
    public <T extends RakNetPacket> T getPacket(byte pid, ByteBuf content) {
        return (T) pool.get(pid).newPacket(content);
    }

    @Override
    protected CompletableFuture<Void> bindInternally() {
        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID);
        final int threads = 1;

        log.info("Loading network with " + threads + " netty threads and with a " + ResourceLeakDetector.getLevel() + " leak level.");
        RakNetProtocol.initialize();

        final CompletableFuture<?>[] bindResults = new CompletableFuture[threads];
        for (int i = 0; i < threads; i++) {
            final CompletableFuture<Void> result = new CompletableFuture<>();
            // create a datagram handler for each thread, then listen for when its complete.
            bootstrap.handler(handler).bind(bindTo.getAddress(), bindTo.getPort()).addListener(future -> {
                if (future.cause() != null) {
                    result.completeExceptionally(future.cause());
                }
                result.complete(null);

            });
            bindResults[i] = result;
        }

        return CompletableFuture.allOf(bindResults);
    }

    @Override
    protected void onTick() {
        final long now = System.currentTimeMillis();

        sessions
                .values()
                .forEach(session -> session.tickSession(now));
    }

    @Override
    protected void handleIncomingPing(ChannelHandlerContext context, InetSocketAddress sender, ClientUnconnectedPing packet) {
        if (!packet.verifyMagic()) return; // invalid magic, leave!

        // retrieve the response needed.
        final BedrockServerPing response = eventHandler.onIncomingPing();

        // respond with the pong.
        final long clientTime = packet.time();
        final ServerUnconnectedPong pong = new ServerUnconnectedPong(clientTime, guid, response.toString());

        // send off
        pong.encodeAndWriteTo(context, sender);
    }

    @Override
    protected void handleIncomingConnectionRequest(ChannelHandlerContext context, InetSocketAddress sender, ByteBuf content) {
        // verify we can read magic first.
        if (!content.isReadable(16)) return;

        // decode and verify magic.
        final ClientOpenConnectionRequest1 packet = getPacket(RakNetProtocol.OPEN_CONNECTION_REQUEST_1, content);
        if (!packet.verifyMagic()) return;

        // verify protocol version.
        if (!packet.verifyProtocolVersion()) {
            sendIncompatibleProtocol(context, sender);
            return;
        }

        // verify we have connections.
        if (sessions.size() >= MAX_CONNECTION_LIMIT) {
            sendNoFreeIncomingConnections(context, sender);
            return;
        }

        // make sure this session doesn't already exist.
        if (sessions.containsKey(sender)) {
            final RakNetServerSession session = sessions.get(sender);
            if (session.getState() != RakNetConnectionState.CONNECTED) {
                // try re-sending this packet.
                session.sendOpenConnectionReply1();
            } else {
                sendAlreadyConnected(context, sender);
            }
            return;
        }

        // https://github.com/CloudburstMC/Network/blob/607c81b9b4194d1bdadb2801eb795ca061081a4f/raknet/src/main/java/com/nukkitx/network/raknet/RakNetServer.java#L164
        // calculate MTU, thank you Nukkit because I don't understand this.
        final int mtu = content.readableBytes() + 1 + 16 + 1 + (sender.getAddress() instanceof Inet6Address ? 40 : 20) + 8;

        // finally, initialize a new session
        final RakNetServerSession session = new RakNetServerSession(this, sender, context.channel(), mtu);
        sessions.put(sender, session);

        log.info("Initialized a new RakNet session from: {}", sender);
        session.sendOpenConnectionReply1();
    }

    @Override
    public void close() {
        super.close();

        sessions
                .values()
                .forEach(RakNetSession::disconnect);
    }

    /**
     * Remove a session
     *
     * @param session the session
     */
    public void removeSession(RakNetServerSession session) {
        this.sessions.remove(session.getAddress(), session);
    }

    /**
     * Handles incoming {@link io.netty.channel.socket.DatagramPacket}'s
     */
    @ChannelHandler.Sharable
    private final class ServerDatagramHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            // initially, ignore anything that could be invalid.
            if (!(msg instanceof final DatagramPacket datagram)) return;
            if (!datagram.content().isReadable()) return;

            try {
                final ByteBuf content = datagram.content();
                final InetSocketAddress sender = datagram.sender();
                final byte pid = (content.readByte());

                // initially, handle any incoming ping requests and connection requests.
                switch (pid) {
                    case RakNetProtocol.UNCONNECTED_PING -> RakNetServer.this.handleIncomingPing(ctx, sender, getPacket(pid, content));
                    case RakNetProtocol.OPEN_CONNECTION_REQUEST_1 -> RakNetServer.this.handleIncomingConnectionRequest(ctx, sender, content);
                    default -> {
                        final RakNetServerSession session = sessions.get(sender);
                        if (session == null) return;

                        if (session.getEventLoop().inEventLoop()) {
                            session.handleDatagram(content);
                        } else {
                            session.getEventLoop().execute(() -> session.handleDatagram(content));
                        }
                    }
                }
            } finally {
                datagram.release();
            }
        }
    }

}
