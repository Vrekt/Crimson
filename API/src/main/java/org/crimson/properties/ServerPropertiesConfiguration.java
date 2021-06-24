package org.crimson.properties;

import java.nio.file.Path;

/**
 * Represents the server.properties file configuration.
 */
public interface ServerPropertiesConfiguration {

    /**
     * Generate the server.properties file.
     *
     * @param where the file path to generate them
     * @return {@code true} if generated was successful.
     */
    boolean generateServerPropertiesIfNeeded(Path where);

    /**
     * Load the server.properties file.
     *
     * @param where the file path to read from
     * @return {@code true} if loading was successful.
     */
    boolean loadServerProperties(Path where);

    /**
     * Get a server property as a string.
     *
     * @param name the name
     * @return the property
     */
    String getPropertyAsString(String name);

    /**
     * Get a server property as a integer.
     *
     * @param name the name
     * @return the property
     */
    int getPropertyAsInteger(String name);

    /**
     * Get a server property as a boolean.
     *
     * @param name the name
     * @return the property
     */
    boolean getPropertyAsBoolean(String name);
}
