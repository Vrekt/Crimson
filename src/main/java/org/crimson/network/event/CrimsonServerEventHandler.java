package org.crimson.network.event;

import org.crimson.Server;
import org.crimson.network.Protocol;
import org.crimson.v3.RakEventHandler;
import org.crimson.v3.utility.BedrockServerPing;

/**
 * Handles incoming RakNet events
 * <p>
 * TODO: Interval refresh ping.
 */
public final class CrimsonServerEventHandler implements RakEventHandler {

    /**
     * Server ping.
     */
    private final BedrockServerPing serverPing;

    /**
     * Initialize
     *
     * @param server the server
     */
    public CrimsonServerEventHandler(Server server) {
        serverPing = new BedrockServerPing()
                .edition("MCPE")
                .motd(server.getProperties().getPropertyAsString("motd"))
                .protocolVersion(Protocol.PROTOCOL_VERSION)
                .versionName(Protocol.PROTOCOL_VERSION_NAME)
                .playerCount(0)
                .maxPlayerCount(server.getProperties().getPropertyAsInteger("max-players"))
                .subMotd(server.getProperties().getPropertyAsString("sub-motd"))
                .gameMode("Survival")
                .gameModeNumber(1)
                .portV4(19132)
                .portV6(19133);

        serverPing.refresh();
    }

    @Override
    public BedrockServerPing onPing() {
        return serverPing;
    }
}
