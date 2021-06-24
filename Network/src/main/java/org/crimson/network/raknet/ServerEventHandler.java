package org.crimson.network.raknet;

import org.crimson.network.utility.BedrockServerPing;

import java.net.InetSocketAddress;

/**
 * Represents a basic handler for the {@link RakNetServer}
 */
public interface ServerEventHandler {

    /**
     * Invoked when the {@link RakNetServer} is initialized
     *
     * @param guid the server GUID.
     */
    void onInitialized(long guid);

    /**
     * @return the provided {@link BedrockServerPing} response.
     */
    BedrockServerPing onIncomingPing();

    /**
     * @param address the address attempting to connect.
     * @return if the provided {@code address} if allowed to connect.
     */
    boolean onConnectionRequest(InetSocketAddress address);

}
