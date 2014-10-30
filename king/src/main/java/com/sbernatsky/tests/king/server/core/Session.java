package com.sbernatsky.tests.king.server.core;

/** Keeps information about logged user and connection expiration time. */
public class Session {
    private final User user;
    private final long expirationTime;

    public Session(User user, long expirationTime) {
        this.user = user;
        this.expirationTime = expirationTime;
    }

    public User getUser() {
        return user;
    }

    public boolean isExpired(long now) {
        return now >= expirationTime;
    }

}
