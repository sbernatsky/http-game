package com.sbernatsky.tests.king.server.core;

/** Represents logged in user. */
public class User {
    private final int id;

    public User(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof User)) {
            return false;
        }

        return this.id == ((User) obj).id;
    }
}
