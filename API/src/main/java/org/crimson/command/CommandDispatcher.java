package org.crimson.command;

import org.crimson.command.sender.CommandSender;

/**
 * Handles dispatching commands from the console.
 */
public interface CommandDispatcher {

    /**
     * Register a default server command.
     *
     * @param command the command
     * @return success or failure.
     */
    boolean registerServerCommand(ServerCommand command);

    /**
     * Check if the provided input is a command
     *
     * @param name the name
     * @return {@code true} if so
     */
    boolean isCommand(String name);

    /**
     * Dispatch the command
     *
     * @param command   the command
     * @param arguments the arguments
     * @param sender    the sender
     */
    void dispatch(String command, String[] arguments, CommandSender sender);

}
