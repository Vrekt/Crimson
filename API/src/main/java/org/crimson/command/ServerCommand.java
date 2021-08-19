package org.crimson.command;

import org.crimson.command.sender.CommandSender;
import org.crimson.permission.Permissible;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a server command.
 */
public abstract class ServerCommand {

    /**
     * Permissions required to execute thie command.
     */
    private final List<String> permissionsRequired = new ArrayList<>();

    /**
     * The name of the server command.
     */
    private final String name;

    public ServerCommand(String name) {
        this.name = name;
    }

    /**
     * Set a permission is required
     *
     * @param permissions the permissions
     */
    protected void setPermissionRequired(String... permissions) {
        this.permissionsRequired.addAll(Arrays.asList(permissions));
    }

    /**
     * Check if the provided permissible has permission to run this command
     *
     * @param permissible the permissible
     * @return {@code true} if so
     */
    public boolean hasPermissionRequired(Permissible permissible) {
        return permissionsRequired.stream().anyMatch(permissible::hasPermission);
    }

    /**
     * Execute
     *
     * @param arguments the args
     * @param sender    the sender
     */
    public abstract void execute(String[] arguments, CommandSender sender);

    /**
     * @return the command name.
     */
    public String getName() {
        return name;
    }
}
