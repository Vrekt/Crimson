package org.crimson.network.raknet.reliability;

/**
 * Represents reliability.
 * <p>
 * References:
 * https://github.com/CloudburstMC/Network/blob/607c81b9b4194d1bdadb2801eb795ca061081a4f/raknet/src/main/java/com/nukkitx/network/raknet/RakNetReliability.java
 * http://www.jenkinssoftware.com/raknet/manual/reliabilitytypes.html
 */
public enum RakNetReliability {

    UNRELIABLE(false, false, false),
    UNRELIABLE_SEQUENCED(false, false, true),
    RELIABLE(true, false, false),
    RELIABLE_ORDERED(true, true, false),
    RELIABLE_SEQUENCED(true, false, true),
    UNRELIABLE_WITH_ACK_RECEIPT(false, false, false),
    RELIABLE_WITH_ACK_RECEIPT(true, false, false),
    RELIABLE_ORDERED_WITH_ACK_RECEIPT(true, true, false);

    /**
     * The properties of each reliability.
     */
    private boolean reliable, ordered, sequenced;

    RakNetReliability(boolean reliable, boolean ordered, boolean sequenced) {
        this.reliable = reliable;
        this.ordered = ordered;
        this.sequenced = sequenced;
    }

    /**
     * @return {@code true} if its reliable.
     */
    public boolean isReliable() {
        return reliable;
    }

    /**
     * @return {@code true} if its ordered.
     */
    public boolean isOrdered() {
        return ordered;
    }

    /**
     * @return {@code true} if its sequenced.
     */
    public boolean isSequenced() {
        return sequenced;
    }

    /**
     * Set of values
     */
    private static final RakNetReliability[] VALUES = values();

    /**
     * Get a {@link RakNetReliability} enum.
     *
     * @param id the ID.
     * @return the reliability or {@code null}
     */
    public static RakNetReliability get(int id) {
        if (id < 0 || id > 7) return null;
        return VALUES[id];
    }

}
