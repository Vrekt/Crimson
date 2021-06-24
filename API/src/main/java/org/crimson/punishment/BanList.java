package org.crimson.punishment;

import java.util.Date;

/**
 * Represents a ban list of players.
 * Either by IP address or name.
 */
public interface BanList {

    /**
     * Add a ban to this list.
     *
     * @param playerName the player name
     * @param ipAddress  the ip address
     * @param reason     the reason
     * @param expiration when the ban expires.
     */
    void addBan(String playerName, String ipAddress, String reason, Date expiration);

    /**
     * Check if the provided {@code addressOrPlayerName} is banned.
     *
     * @param addressOrPlayerName the IP address or player name.
     * @return {@code true} if the player is banned.
     */
    boolean isBanned(String addressOrPlayerName);

}
