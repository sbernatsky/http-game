package com.sbernatsky.tests.king.server.core;


/** Usually users are stored somewhere and are loaded through some service. */
public class UserRegistry {

    public User getOrCreateUser(int id) {
        return new User(id);
    }
}
