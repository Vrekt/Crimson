package org.crimson.punishment;

import com.google.common.base.Preconditions;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Represents a ban list that can only handle or include player names.
 */
public final class BanListByName implements BanList {

    /**
     * Set of names that are currently banned.
     * <p>
     * This implementation uses a {@link CopyOnWriteArrayList} for fast read performance.
     */
    private final List<String> names = new CopyOnWriteArrayList<>();

    @Override
    public void addBan(String playerName, String ipAddress, String reason, Date expiration) {
        Preconditions.checkNotNull(playerName, "The player name may not be null.");
    }

    @Override
    public boolean isBanned(String addressOrPlayerName) {
        return names.contains(addressOrPlayerName);
    }
}

