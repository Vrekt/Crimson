package org.crimson.punishment;

import com.google.common.base.Preconditions;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Represents a ban list that can only handle or include IP addresses.
 */
public final class BanListByIP implements BanList {

    /**
     * Set of IP addresses that are currently banned.
     * <p>
     * This implementation uses a {@link CopyOnWriteArrayList} for fast read performance.
     */
    private final List<String> addresses = new CopyOnWriteArrayList<>();

    @Override
    public void addBan(String playerName, String ipAddress, String reason, Date expiration) {
        Preconditions.checkNotNull(ipAddress, "The ip address may not be null.");
    }

    @Override
    public boolean isBanned(String addressOrPlayerName) {
        return addresses.contains(addressOrPlayerName);
    }
}
