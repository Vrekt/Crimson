package org.crimson.properties;

/**
 * Represents the default server properties.
 */
public enum DefaultServerProperties {

    /**
     * The main MOTD line.
     */
    MOTD("motd", "A Crimson server."),

    /**
     * The second MOTD line.
     */
    SUB_MOTD("sub-motd", "A Crimson server!"),

    /**
     * The max players allowed.
     */
    MAX_PLAYERS("max-players", "20");

    /**
     * The name and value.
     */
    protected final String name, value;

    DefaultServerProperties(String name, String value) {
        this.name = name;
        this.value = value;
    }

}
