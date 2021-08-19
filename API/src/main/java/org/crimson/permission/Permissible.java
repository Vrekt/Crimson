package org.crimson.permission;

import java.util.Collection;

/**
 * Permissions base.
 */
public interface Permissible {

    /**
     * If this permission holder is OP.
     *
     * @return {@code true} if so
     */
    boolean isOp();

    /**
     * Set if this permission holder is OP.
     *
     * @param op if this permission holder is OP.
     */
    void setOp(boolean op);

    /**
     * If this permission holder has the provided {@code name} permission
     *
     * @param name the name
     * @return {@code true} if so
     */
    boolean hasPermission(String name);

    /**
     * Set permissions
     *
     * @param permissions the permissions
     */
    void setPermission(String... permissions);

    /**
     * Set permissions
     *
     * @param permissions the permissions
     */
    void setPermissions(Collection<String> permissions);

}
