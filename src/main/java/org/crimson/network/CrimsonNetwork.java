package org.crimson.network;

import lombok.extern.log4j.Log4j2;
import org.crimson.network.event.CrimsonServerEventHandler;
import org.crimson.network.raknet.RakNetServer;

import java.net.InetSocketAddress;

/**
 * The crimson network.
 */
@Log4j2
public final class CrimsonNetwork implements Network {

    /**
     * Rak server
     */
    private final RakNetServer server;

    public CrimsonNetwork(InetSocketAddress address, CrimsonServerEventHandler eventHandler) {
        this.server = new RakNetServer(address);
        this.server.setEventHandler(eventHandler);
    }

    /**
     * Bind.
     */
    public void bind() {
        server.bind().join();
        log.info("Server started at 127.0.0.1 on port 19132");
    }

}
