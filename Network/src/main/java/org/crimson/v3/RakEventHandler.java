package org.crimson.v3;

import org.crimson.v3.utility.BedrockServerPing;

/**
 * Handle RakNet events.
 */
public interface RakEventHandler {

    /**
     * Invoked when a ping is received.
     *
     * @return the server ping response.
     */
    BedrockServerPing onPing();

}
