package org.crimson.permission;

import java.util.List;

/**
 * System permissions
 * <p>
 * TODO: Config: "op-has-all-permissions: true/false"
 */
public interface CrimsonPermissions {

    /**
     * Allows the sender to stop the server.
     */
    String SERVER_STOP = "crimson.server.stop";

    /**
     * Set of all permissions.
     */
    List<String> ALL_PERMISSIONS = List.of(SERVER_STOP);

}
