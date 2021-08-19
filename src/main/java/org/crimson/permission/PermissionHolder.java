package org.crimson.permission;

import java.util.*;

/**
 * Keeps track of permissions.
 * <p>
 * TODO: Will be improved in the future.
 */
public final class PermissionHolder implements Permissible {

    /**
     * Set of permissions
     */
    private final List<String> permissions = new ArrayList<>();

    /**
     * If we are OP.
     */
    private boolean op;

    @Override
    public boolean isOp() {
        return op;
    }

    @Override
    public void setOp(boolean op) {
        this.op = op;
    }

    @Override
    public boolean hasPermission(String name) {
        return this.permissions.contains(name);
    }

    @Override
    public void setPermission(String... permissions) {
        this.permissions.addAll(Arrays.asList(permissions));
    }

    @Override
    public void setPermissions(Collection<String> permissions) {
        this.permissions.addAll(permissions);
    }
}
