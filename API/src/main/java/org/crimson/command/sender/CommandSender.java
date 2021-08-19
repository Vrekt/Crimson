package org.crimson.command.sender;

import org.crimson.permission.Permissible;

/**
 * Represents somebody who can send commands.
 */
public interface CommandSender extends Permissible {

    /**
     * Send a message to whoever sent the command
     *
     * @param message the message
     */
    void sendMessage(String message);

}
