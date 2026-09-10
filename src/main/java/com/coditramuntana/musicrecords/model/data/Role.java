package com.coditramuntana.musicrecords.model.data;

/**
 * Role granted to a {@link UserAccount}.
 *
 * <p>The name stored in the database is the plain enum name; the {@code ROLE_} prefix that
 * Spring Security expects is added when building the authorities, so the persisted values
 * stay readable.
 */
public enum Role {

    ADMIN,
    USER;

    /**
     * Authority name expected by Spring Security's {@code hasRole} checks.
     */
    public String authority() {
        return "ROLE_" + name();
    }

}
