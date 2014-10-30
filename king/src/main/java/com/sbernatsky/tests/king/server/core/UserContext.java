package com.sbernatsky.tests.king.server.core;

/** Keeps current logged in user. Similar to spring security. So consumers may get it without polluting their interfaces. */
public class UserContext {
    private static final ThreadLocal<User> USER_HOLDER = new ThreadLocal<>();

    public static User getCurrentUser() {
        return USER_HOLDER.get();
    }
    public static void clear() {
        USER_HOLDER.remove();
    }
    public static void setCurrentUser(User user) {
        USER_HOLDER.set(user);
    }
}
