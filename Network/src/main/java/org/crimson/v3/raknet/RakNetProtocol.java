package org.crimson.v3.raknet;

import io.netty.buffer.ByteBuf;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetSocketAddress;
import java.util.Arrays;

/**
 * Represents constants within the RakNet protocol.
 */
public interface RakNetProtocol {

    /**
     * https://github.com/CloudburstMC/Network/blob/607c81b9b4194d1bdadb2801eb795ca061081a4f/raknet/src/main/java/com/nukkitx/network/raknet/RakNetConstants.java#L22
     */
    int MAXIMUM_ENCAPSULATED_HEADER_SIZE = 28;
    int UDP_HEADER_SIZE = 8;
    int DATAGRAM_HEADER_SIZE = 4;

    /**
     * 0x00 ff ff 00 fe fe fe fe fd fd fd fd 12 34 56 78
     * RakNet magic values
     */
    byte[] MAGIC = new byte[]{0, -1, -1, 0, -2, -2, -2, -2, -3, -3, -3, -3, 18, 52, 86, 120};

    /**
     * Check if the provided buffer (reading the next 16 bytes) is magic.
     *
     * @param buffer the buffer
     * @return {@code true} if so
     */
    static boolean isMagic(ByteBuf buffer) {
        final byte[] magic = new byte[16];
        buffer.readBytes(magic, 0, 16);
        return Arrays.equals(magic, MAGIC);
    }

    /**
     * Valid flag.
     */
    byte VALID = (byte) 0b10000000;

    /**
     * Reliability bit flag
     */
    byte RELIABILITY = (byte) 0b11100000;

    /**
     * Split bit flag
     */
    byte SPLIT = (byte) 0b00010000;

    /**
     * Send bit flag
     */
    byte CONTINUOUS_SEND = (byte) 0b00001000;

    /**
     * The current version of the protocol.
     */
    int PROTOCOL_VERSION = 10;

    /**
     * The UNCONNECTED_PING.
     * Sent by clients attempting to retrieve MOTD of servers.
     */
    byte UNCONNECTED_PING = 0x01;

    /**
     * The UNCONNECTED_PONG
     * Sent by the server in response to {@code UNCONNECTED_PING}
     */
    byte UNCONNECTED_PONG = 0x1c;

    /**
     * The first open connection request.
     */
    byte OPEN_CONNECTION_REQUEST_1 = 0x05;

    /**
     * Incompatible RakNet protocol version.
     */
    byte INCOMPATIBLE_PROTOCOL = 0x19;

    /**
     * Connection banned
     */
    byte CONNECTION_BANNED = 0x017;

    /**
     * Indicates there is no free connections left.
     */
    byte NO_FREE_INCOMING_CONNECTIONS = 0x14;

    /**
     * Indicates the sender is already connected.
     */
    byte ALREADY_CONNECTED = 0x12;

    /**
     * The first open connection reply.
     */
    byte OPEN_CONNECTION_REPLY_1 = 0x06;

    /**
     * Sent to indicate a disconnection.
     */
    byte DISCONNECTED = 0x15;

    /**
     * The second open connection request
     */
    byte OPEN_CONNECTION_REQUEST_2 = 0x07;

    /**
     * The second open connection reply
     */
    byte OPEN_CONNECTION_REPLY_2 = 0x08;

    /**
     * A connection request
     */
    byte CONNECTION_REQUEST = 0x09;

    /**
     * Connection request failed or was denied.
     */
    byte CONNECTION_REQUEST_FAILED = 0x11;

    /**
     * The connection request was accepted.
     */
    byte CONNECTION_REQUEST_ACCEPTED = 0x10;

    /**
     * Clamp MTU size
     *
     * @param mtu mtu
     */
    static int clampMtuSize(int mtu) {
        return Math.max(576, Math.min(1400, mtu));
    }

    /**
     * Check if the potential byte ID is RakNet.
     *
     * @param potential the ID
     * @return {@code true} if so
     */
    static boolean isRakNet(byte potential) {
        return (potential & VALID) != 0;
    }

    /**
     * Write a IPv4 or IPv6 address.
     * https://github.com/NukkitX/Network/blob/79a77142ab3e34d9f45170dec1e6f906c9cf53ca/common/src/main/java/com/nukkitx/network/NetworkUtils.java#L40
     *
     * @param address the address.
     * @param buffer  the buffer
     */
    static void writeAddress(InetSocketAddress address, ByteBuf buffer) {
        final var add = address.getAddress();
        final var bytes = address.getAddress().getAddress();
        if (add instanceof Inet4Address) {
            buffer.writeByte(4);
            for (byte aByte : bytes) buffer.writeByte((byte) (~aByte & 0xFF));
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

    /**
     * https://github.com/CloudburstMC/Network/blob/607c81b9b4194d1bdadb2801eb795ca061081a4f/raknet/src/main/java/com/nukkitx/network/raknet/RakNetConstants.java#L98
     */
    InetSocketAddress LOOPBACK_V4 = new InetSocketAddress(Inet4Address.getLoopbackAddress(), 19132);
    InetSocketAddress LOOPBACK_V6 = new InetSocketAddress(Inet6Address.getLoopbackAddress(), 19132);
    InetSocketAddress[] LOCAL_IP_ADDRESSES_V4 = new InetSocketAddress[20];
    InetSocketAddress[] LOCAL_IP_ADDRESSES_V6 = new InetSocketAddress[20];

    /**
     * Initialize
     */
    static void initialize() {
        LOCAL_IP_ADDRESSES_V4[0] = LOOPBACK_V4;
        LOCAL_IP_ADDRESSES_V6[0] = LOOPBACK_V6;

        for (int i = 1; i < 20; i++) {
            LOCAL_IP_ADDRESSES_V4[i] = new InetSocketAddress("0.0.0.0", 19132);
            LOCAL_IP_ADDRESSES_V6[i] = new InetSocketAddress("::0", 19132);
        }
    }

}
