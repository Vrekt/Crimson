package org.crimson.network.raknet.session.state;

/**
 * Represents a connection state.
 */
public enum RakNetConnectionState {

    /**
     * The sender is unconnected.
     */
    UNCONNECTED,

    /**
     * The sender is initializing
     */
    INITIALIZING,

    /**
     * RakNet has been initialized
     */
    INITIALIZED,

    /**
     * Connecting
     */
    CONNECTING,

    /**
     * The sender is connected
     */
    CONNECTED

}
