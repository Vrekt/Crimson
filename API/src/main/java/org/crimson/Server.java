package org.crimson;

import org.crimson.network.Network;
import org.crimson.properties.ServerPropertiesConfiguration;
import org.crimson.punishment.BanList;
import org.crimson.punishment.BanListType;

/**
 * Represents the Crimson server.
 */
public interface Server {

    /**
     * Get the corresponding ban list.
     *
     * @param type the type
     * @return the {@link BanList}
     */
    BanList getBanList(BanListType type);

    /**
     * Get the server.properties.
     *
     * @return the server.properties file configuration.
     */
    ServerPropertiesConfiguration getProperties();

    /**
     * Get the networking component.
     *
     * @return the network.
     */
    Network getNetwork();

    /**
     * @return the version
     */
    String getVersion();

    /**
     * Shutdown the server.
     */
    void shutdown();

}
