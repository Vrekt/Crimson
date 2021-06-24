package org.crimson.network.event;

import org.crimson.Server;
import org.crimson.network.Protocol;
import org.crimson.network.raknet.ServerEventHandler;
import org.crimson.network.utility.BedrockServerPing;
import org.crimson.punishment.BanListType;

import java.net.InetSocketAddress;

/**
 * Handles incoming RakNet events
 */
public final class CrimsonServerEventHandler implements ServerEventHandler {

    /**
     * The server instance
     */
    private final Server server;

    /**
     * The server ping response
     */
    private final BedrockServerPing serverPing;

    /**
     * Initialize
     *
     * @param server the server instance
     */
    public CrimsonServerEventHandler(Server server) {
        this.server = server;

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
    }

    @Override
    public void onInitialized(long guid) {
        serverPing.guid(guid).refresh();
    }

    @Override
    public BedrockServerPing onIncomingPing() {
        return serverPing;
    }

    @Override
    public boolean onConnectionRequest(InetSocketAddress address) {
        return !server.getBanList(BanListType.IP).isBanned(address.getHostName());
    }
}
