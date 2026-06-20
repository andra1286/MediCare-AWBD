package com.medicare.common.security;

/**
 * Shared constants for JWT handling so every service uses the same header and claim names.
 */
public final class SecurityConstants {

    private SecurityConstants() {
    }

    /** HTTP header that carries the bearer token. */
    public static final String AUTH_HEADER = "Authorization";

    /** Prefix used in the Authorization header value. */
    public static final String BEARER_PREFIX = "Bearer ";

    /** JWT claim that stores the list of role names. */
    public static final String ROLES_CLAIM = "roles";

    /** JWT claim that stores the numeric user id. */
    public static final String USER_ID_CLAIM = "userId";

    /** Header the gateway uses to forward the authenticated username downstream. */
    public static final String USER_HEADER = "X-Auth-User";
}
