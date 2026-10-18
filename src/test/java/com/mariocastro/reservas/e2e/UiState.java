package com.mariocastro.reservas.e2e;

import com.mariocastro.reservas.e2e.support.TestUsers;

public final class UiState {

    private static final ThreadLocal<TestUsers.User> USER = new ThreadLocal<>();

    private UiState() {
    }

    public static TestUsers.User user() {
        TestUsers.User user = USER.get();
        if (user == null) {
            throw new IllegalStateException("No user in this scenario: a Given step must create one first");
        }
        return user;
    }

    public static void setUser(TestUsers.User user) {
        USER.set(user);
    }

    static void clear() {
        USER.remove();
    }
}
