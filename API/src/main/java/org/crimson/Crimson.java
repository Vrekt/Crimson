package org.crimson;

/**
 * Utility class and accessors
 */
public final class Crimson {

    /**
     * The server
     */
    private static Server server;

    private Crimson() {
        throw new UnsupportedOperationException();
    }

    /**
     * Attempts to set the server
     *
     * @param server the server
     */
    public static void setServer(Server server) {
        if (Crimson.server != null) throw new UnsupportedOperationException("Server instance is already set.");
        Crimson.server = server;
    }

    /**
     * @return the server singleton
     */
    public static Server getServer() {
        return server;
    }

}
