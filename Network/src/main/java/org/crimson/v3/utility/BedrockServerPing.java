package org.crimson.v3.utility;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Represents a ping or MOTD response for a client.
 * <p>
 * Edition (MCPE or MCEE for Education Edition);MOTD line 1;Protocol Version;Version Name;Player Count;Max Player Count;Server Unique ID;MOTD line 2;Game mode;Game mode (numeric);Port (IPv4);Port (IPv6);
 */
public final class BedrockServerPing {

    /**
     * The server edition
     * MOTD 1
     * The version name
     * MOTD 2
     * The gameMode name
     */
    private String edition, motd, versionName, subMotd, gameMode;

    /**
     * The current protocol version
     * The current player count
     * The max player count
     * The gameMode number
     * the V4/V6 ports.
     */
    private int protocolVersion, playerCount, maxPlayerCount, gameModeNumber, portV4, portV6;

    /**
     * Server ID.
     */
    private long guid;

    /**
     * The current cached response.
     */
    private final AtomicReference<byte[]> response = new AtomicReference<>();

    /**
     * String builder
     */
    private final StringBuilder builder = new StringBuilder();

    /**
     * Set the edition
     *
     * @param edition the edition
     * @return this
     */
    public BedrockServerPing edition(String edition) {
        this.edition = edition;
        return this;
    }

    /**
     * Set the motd
     *
     * @param motd the motd
     * @return this
     */
    public BedrockServerPing motd(String motd) {
        this.motd = motd;
        return this;
    }

    /**
     * Set the version name
     *
     * @param versionName the name
     * @return this
     */
    public BedrockServerPing versionName(String versionName) {
        this.versionName = versionName;
        return this;
    }

    /**
     * Set the sub-motd
     *
     * @param subMotd subMotd
     * @return this
     */
    public BedrockServerPing subMotd(String subMotd) {
        this.subMotd = subMotd;
        return this;
    }

    /**
     * Set the gameMode
     *
     * @param gameMode the gameMode
     * @return this
     */
    public BedrockServerPing gameMode(String gameMode) {
        this.gameMode = gameMode;
        return this;
    }

    /**
     * Set the protocol version
     *
     * @param protocolVersion protocolVersion
     * @return this
     */
    public BedrockServerPing protocolVersion(int protocolVersion) {
        this.protocolVersion = protocolVersion;
        return this;
    }

    /**
     * Set the playerCount
     *
     * @param playerCount the player count
     * @return this
     */
    public BedrockServerPing playerCount(int playerCount) {
        this.playerCount = playerCount;
        return this;
    }

    /**
     * Set the max player count
     *
     * @param maxPlayerCount max player count
     * @return this
     */
    public BedrockServerPing maxPlayerCount(int maxPlayerCount) {
        this.maxPlayerCount = maxPlayerCount;
        return this;
    }

    /**
     * Set game mode number
     *
     * @param gameModeNumber number
     * @return this
     */
    public BedrockServerPing gameModeNumber(int gameModeNumber) {
        this.gameModeNumber = gameModeNumber;
        return this;
    }

    /**
     * Set the port v4
     *
     * @param portV4 v4
     * @return this
     */
    public BedrockServerPing portV4(int portV4) {
        this.portV4 = portV4;
        return this;
    }

    /**
     * Set the port v6
     *
     * @param portV6 v6
     * @return this
     */
    public BedrockServerPing portV6(int portV6) {
        this.portV6 = portV6;
        return this;
    }

    /**
     * Set the guid
     *
     * @param guid guid
     * @return this
     */
    public BedrockServerPing guid(long guid) {
        this.guid = guid;
        return this;
    }

    /**
     * Retrieve the ping response
     *
     * @return the response as bytes.
     */
    public byte[] getResponse() {
        return response.get();
    }

    /**
     * Refresh the ping response.
     */
    public void refresh() {
        builder.setLength(0);

        append(edition);
        append(motd);
        append(protocolVersion);
        append(versionName);
        append(playerCount);
        append(maxPlayerCount);
        append(guid);
        append(subMotd);
        append(gameMode);
        append(gameModeNumber);
        append(portV4);
        append(portV6);

        response.set(builder.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Append
     *
     * @param what the string
     */
    private void append(String what) {
        builder.append(what).append(";");
    }

    /**
     * Append
     *
     * @param what the int
     */
    private void append(int what) {
        builder.append(what).append(";");
    }

    /**
     * Append
     *
     * @param what the int
     */
    private void append(long what) {
        builder.append(what).append(";");
    }

}
