package org.crimson.command.commands;

import org.crimson.Crimson;
import org.crimson.command.ServerCommand;
import org.crimson.command.sender.CommandSender;
import org.crimson.permission.CrimsonPermissions;

/**
 * A server command for stopping the server.
 */
public final class CommandServerStop extends ServerCommand {

    public CommandServerStop() {
        super("stop");

        setPermissionRequired(CrimsonPermissions.SERVER_STOP);
    }

    @Override
    public void execute(String[] arguments, CommandSender sender) {
        sender.sendMessage("Stopping server...");
        Crimson.getServer().shutdown();
    }
}
