package org.crimson.network2.raknet.session;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import lombok.extern.log4j.Log4j2;
import org.crimson.network2.raknet.RakNetServer;
import org.crimson.network2.raknet.packet.RakNetPacket;
import org.crimson.network2.raknet.packet.client.ClientConnectionRequest;
import org.crimson.network2.raknet.packet.client.ClientOpenConnectionRequest2;
import org.crimson.network2.raknet.packet.datagram.RakNetDatagram;
import org.crimson.network2.raknet.priority.RakNetPriority;
import org.crimson.network2.raknet.protocol.RakNetProtocol;
import org.crimson.network2.raknet.reliability.RakNetReliability;
import org.crimson.network2.raknet.session.state.RakNetConnectionState;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetSocketAddress;

/**
 * Represents a session within the {@link RakNetServer}
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
        final boolean v6 = address.getAddress() instanceof Inet6Address;
        final ByteBuf buf = channel.alloc().ioBuffer(v6 ? 628 : 166);

        buf.writeByte(RakNetProtocol.CONNECTION_REQUEST_ACCEPTED);
        writeAddress(buf, address);
        buf.writeShort(0);

        for (int i = 0; i < 10; i++) {
            writeAddress(buf, new InetSocketAddress("255.255.255.255", 19132));
        }

        buf.writeLong(time);
        buf.writeLong(System.currentTimeMillis());

        send(buf, RakNetPriority.IMMEDIATE, RakNetReliability.RELIABLE);
    }

    /**
     * Write a IPv4 or IPv6 address.
     * https://github.com/NukkitX/Network/blob/79a77142ab3e34d9f45170dec1e6f906c9cf53ca/common/src/main/java/com/nukkitx/network/NetworkUtils.java#L40
     *
     * @param address the address.
     */
    public void writeAddress(ByteBuf buffer, InetSocketAddress address) {
        final var add = address.getAddress();
        final var bytes = address.getAddress().getAddress();
        if (add instanceof Inet4Address) {
            buffer.writeByte(4);
            for (int i = 0; i < bytes.length; i++) bytes[i] = (byte) (~bytes[i] & 0xFF);
            buffer.writeBytes(bytes);
            buffer.writeShort(address.getPort());
        } else if (add instanceof Inet6Address) {
            buffer.writeByte(6);
            buffer.writeShortLE(23);
            buffer.writeShort(address.getPort());
            buffer.writeInt(0);
            buffer.writeBytes(bytes);
            buffer.writeInt(((Inet6Address) add).getScopeId());
        }


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
        final ClientConnectionRequest packet = server.getPacket(RakNetProtocol.CONNECTION_REQUEST, content);
        if (packet.getGuid() != guid || packet.isSecurity()) {
            // invalid client, disconnect.
            log.info("Disconnecting {}", address);
            sendConnectionFailed();
            disconnect();
            return;
        }

        log.info("Accepting {}", address);
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
        } else {
            // otherwise, a normal packet.
            this.handlePacket(content);
        }

    }

    @Override
    protected void handleRakNetDatagram(RakNetDatagram datagram, ByteBuf content) {
        datagram.decodePacket(content);

        datagram.getPackets()
                .forEach(packet -> {
                    final int pid = packet.getBuffer().readUnsignedByte();
                    log.info("Processing new EncapsulatedPacket IsSplit: {} ID: {} Reliability: {}", packet.isSplit(), pid, packet.getReliability());

                    this.handlePacket(packet.getBuffer());
                });
    }

    @Override
    protected void close() {
        server.removeSession(this);
    }
}
