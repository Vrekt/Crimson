package org.crimson.network;

import lombok.extern.log4j.Log4j2;
import org.crimson.network.event.CrimsonServerEventHandler;
import org.crimson.v3.RakNetServer;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;

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
        this.server = new RakNetServer(address, eventHandler);
    }

    /**
     * @return the parent server.
     */
    public RakNetServer getServer() {
        return server;
    }

    /**
     * Bind.
     *
     * @return the result.
     */
    public boolean bind() {
        final AtomicBoolean result = new AtomicBoolean(true);

        server.bind().whenComplete((v, error) -> {
            if (error != null) {
                log.error("Failed to bind to {}", server.getBindTo() + "!");
                log.error("Is the server address already in use?");
                error.printStackTrace();

                result.set(false);
                return;
            }

            log.info("Successfully bound to {}", server.getBindTo());
        }).join();
        return result.get();
    }

}
