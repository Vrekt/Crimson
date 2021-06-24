package org.crimson.network.raknet.session;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import lombok.extern.log4j.Log4j2;
import org.crimson.network.raknet.RakNetServer;
import org.crimson.network.raknet.packet.RakNetPacket;
import org.crimson.network.raknet.packet.client.ClientConnectionRequest;
import org.crimson.network.raknet.packet.client.ClientOpenConnectionRequest2;
import org.crimson.network.raknet.packet.datagram.RakNetDatagram;
import org.crimson.network.raknet.protocol.RakNetProtocol;
import org.crimson.network.raknet.session.state.RakNetConnectionState;

import java.net.InetSocketAddress;

/**
 * Represents a session within the {@link org.crimson.network.raknet.RakNetServer}
 */
@Log4j2
public final class RakNetServerSession extends RakNetSession {

    public RakNetServerSession(RakNetServer server, InetSocketAddress address, Channel channel, int mtu) {
        super(server, address, channel, mtu);
    }

    @Override
    public void sendOpenConnectionReply1() {
        if (this.state != RakNetConnectionState.INITIALIZING) return;

        RakNetPacket.createPacket(channel.alloc().ioBuffer(28, 28), RakNetProtocol.OPEN_CONNECTION_REPLY_1)
                .writeId()
                .writeMagic()
                .writeLong(server.getServerId())
                .writeBoolean(false)
                .writeShort(mtu)
                .encodeAndWriteTo(channel, address);

        update();
    }

    @Override
    public void sendOpenConnectionReply2() {
        if (this.state != RakNetConnectionState.INITIALIZING) return;

        RakNetPacket.createPacket(channel.alloc().ioBuffer(31), RakNetProtocol.OPEN_CONNECTION_REPLY_2)
                .writeId()
                .writeMagic()
                .writeLong(server.getServerId())
                .writeAddress(address)
                .writeShort(mtu)
                .writeBoolean(false)
                .encodeAndWriteTo(channel, address);

        update();
    }

    @Override
    protected void sendConnectionRequestAccepted(long time) {
        // TODO:
    }

    @Override
    public void handleSecondConnectionRequest(ByteBuf content) {
        if (this.state != RakNetConnectionState.INITIALIZING) return;
        final ClientOpenConnectionRequest2 packet = server.getPacket(RakNetProtocol.OPEN_CONNECTION_REQUEST_2, content);
        if (!packet.verifyMagic()) return;

        this.mtu = packet.getMtu();
        this.guid = packet.getGuid();

        sendOpenConnectionReply2();
        this.state = RakNetConnectionState.INITIALIZED;
    }

    @Override
    protected void handleConnectionRequest(ByteBuf content) {
        if (this.state != RakNetConnectionState.INITIALIZED) return;

        final ClientConnectionRequest packet = server.getPacket(RakNetProtocol.CONNECTION_REQUEST, content);
        if (packet.getGuid() != guid || packet.isSecurity()) {
            // invalid client, disconnect.
            sendConnectionFailed();
            disconnect();
            return;
        }

        this.state = RakNetConnectionState.CONNECTING;
        sendConnectionRequestAccepted(packet.getTime());
    }

    @Override
    public void handleDatagram(ByteBuf content) {
        if (isClosed) return;
        update();

        content.readerIndex(0);
        final byte potential = content.readByte();
        final boolean isRakNet = (potential & RakNetProtocol.VALID) != 0;

        if (isRakNet) {
            // received a RakNet packet, make sure we have a valid RakNet session first.
            if (this.state.ordinal() >= RakNetConnectionState.INITIALIZED.ordinal()) {
                content.readerIndex(0);

                // decode this new datagram.
                final RakNetDatagram datagram = new RakNetDatagram();
                handleRakNetDatagram(datagram, content);
            }

            // TODO: NACK+ACK
        } else {
            // otherwise, a normal packet.
            this.handlePacket(content);
        }

    }

    @Override
    protected void handleRakNetDatagram(RakNetDatagram datagram, ByteBuf content) {
        datagram.decodePacket(content);

        // TODO: Sliding window.
        datagram.getPackets()
                .forEach(packet -> {
                    final int pid = packet.getBuffer().readUnsignedByte();
                    log.info("Processing new EncapsulatedPacket Split:{} ID:{} Reliablity:{}", packet.isSplit(), pid, packet.getReliability());

                    this.handlePacket(packet.getBuffer());
                });

    }

    @Override
    protected void close() {
        server.removeSession(this);
    }
}
