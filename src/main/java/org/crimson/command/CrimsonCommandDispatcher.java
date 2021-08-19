package org.crimson.command;

import lombok.extern.log4j.Log4j2;
import org.crimson.command.sender.CommandSender;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles registering and dispatching commands from within the console.
 */
@Log4j2
public final class CrimsonCommandDispatcher implements CommandDispatcher {

    /**
     * Map of server commands.
     */
    private final Map<String, ServerCommand> serverCommands = new HashMap<>();

    @Override
    public boolean registerServerCommand(ServerCommand command) {
        if (this.serverCommands.containsKey(command.getName())) return false;
        this.serverCommands.put(command.getName(), command);
        return true;
    }

    @Override
    public boolean isCommand(String name) {
        return serverCommands.containsKey(name.toLowerCase());
    }

    @Override
    public void dispatch(String command, String[] arguments, CommandSender sender) {
        final ServerCommand serverCommand = serverCommands.get(command);
        if (!serverCommand.hasPermissionRequired(sender)) {
            sender.sendMessage("You do not have permission to run this command!");
        } else {
            serverCommand.execute(arguments, sender);
        }
    }
}
