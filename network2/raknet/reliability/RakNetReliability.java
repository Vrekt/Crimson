package org.crimson.network2.raknet.reliability;

/**
 * Represents reliability.
 * <p>
 * References:
 * https://github.com/CloudburstMC/Network/blob/607c81b9b4194d1bdadb2801eb795ca061081a4f/raknet/src/main/java/com/nukkitx/network/raknet/RakNetReliability.java
 * http://www.jenkinssoftware.com/raknet/manual/reliabilitytypes.html
 */
public enum RakNetReliability {

    UNRELIABLE(0, false, false, false),
    UNRELIABLE_SEQUENCED(3, false, false, true),
    RELIABLE(3, true, false, false),
    RELIABLE_ORDERED(7, true, true, false),
    RELIABLE_SEQUENCED(6, true, false, true),
    UNRELIABLE_WITH_ACK_RECEIPT(0, false, false, false),
    RELIABLE_WITH_ACK_RECEIPT(3, true, false, false),
    RELIABLE_ORDERED_WITH_ACK_RECEIPT(7, true, true, false);

    /**
     * The size
     */
    private final int size;

    /**
     * The properties of each reliability.
     */
    private final boolean reliable, ordered, sequenced;

    RakNetReliability(int size, boolean reliable, boolean ordered, boolean sequenced) {
        this.size = size;
        this.reliable = reliable;
        this.ordered = ordered;
        this.sequenced = sequenced;
    }

    /**
     * @return the size
     */
    public int getSize() {
        return size;
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
