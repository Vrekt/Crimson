package org.crimson;

import lombok.extern.log4j.Log4j2;
import org.crimson.network.CrimsonNetwork;
import org.crimson.network.Network;
import org.crimson.network.event.CrimsonServerEventHandler;
import org.crimson.properties.CrimsonServerProperties;
import org.crimson.properties.ServerPropertiesConfiguration;
import org.crimson.punishment.BanList;
import org.crimson.punishment.BanListByIP;
import org.crimson.punishment.BanListByName;
import org.crimson.punishment.BanListType;

import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.Scanner;

/**
 * The base server
 */
@Log4j2
final class CrimsonServer implements Server {

    /**
     * Current path of this server
     */
    private static final String PATH = System.getProperty("user.dir") + "/";

    /**
     * The server.properties file configuration.
     */
    private final ServerPropertiesConfiguration serverProperties = new CrimsonServerProperties();

    /**
     * Constant array of ban-lists.
     */
    private final BanList[] banLists = new BanList[2];

    /**
     * The RakNet server
     */
    private final CrimsonNetwork network;

    public CrimsonServer() {
        if (!loadServerProperties()) shutdown();
        loadPlayerBans();

        network = new CrimsonNetwork(new InetSocketAddress("127.0.0.1", 19132), new CrimsonServerEventHandler(this));
        network.bind();

        new Scanner(System.in).nextLong();
    }

    /**
     * @return the server.properties in memory
     */
    public ServerPropertiesConfiguration getProperties() {
        return serverProperties;
    }

    @Override
    public Network getNetwork() {
        return network;
    }

    /**
     * Load server.properties file.
     *
     * @return the result
     */
    private boolean loadServerProperties() {
        final Path path = Path.of(PATH + "server.properties");
        if (!serverProperties.generateServerPropertiesIfNeeded(path)) return false;
        return serverProperties.loadServerProperties(path);
    }

    /**
     * Initialize the {@code banLists} array and load player bans from the file.
     */
    private void loadPlayerBans() {
        banLists[BanListType.NAME.ordinal()] = new BanListByName();
        banLists[BanListType.IP.ordinal()] = new BanListByIP();
    }

    /**
     * Shutdown
     */
    private void shutdown() {
        System.exit(0);
    }

    @Override
    public BanList getBanList(BanListType type) {
        return banLists[type.ordinal()];
    }
}
