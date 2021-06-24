package org.crimson.network.raknet.protocol;

/**
 * Represents constants within the RakNet protocol.
 */
public interface RakNetProtocol {

    /**
     * 0x00 ff ff 00 fe fe fe fe fd fd fd fd 12 34 56 78
     * RakNet magic values
     */
    byte[] MAGIC = new byte[]{(byte) 0x00, (byte) 0xff,
            (byte) 0xff, (byte) 0x00, (byte) 0xfe, (byte) 0xfe, (byte) 0xfe,
            (byte) 0xfe, (byte) 0xfd, (byte) 0xfd, (byte) 0xfd,
            (byte) 0xfd, (byte) 0x12, (byte) 0x34, (byte) 0x56, (byte) 0x78};

    /**
     * Valid flag.
     */
    byte VALID = (byte) 0b10000000;

    /**
     * Reliability bit flag for {@link org.crimson.network.raknet.packet.encapsulated.EncapsulatedPacket}
     */
    byte RELIABILITY = (byte) 0b11100000;

    /**
     * Reliability bit flag for {@link org.crimson.network.raknet.packet.encapsulated.EncapsulatedPacket}
     */
    byte SPLIT = (byte) 0b00010000;

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

}
