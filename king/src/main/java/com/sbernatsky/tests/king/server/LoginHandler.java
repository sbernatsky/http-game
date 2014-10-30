package com.sbernatsky.tests.king.server;

import java.io.IOException;

import com.sbernatsky.tests.king.server.core.SessionRegistry;
import com.sbernatsky.tests.king.server.core.User;
import com.sbernatsky.tests.king.server.core.UserRegistry;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * Login handler.
 * <p>Loads user id from requert uri, finds user in registry and creates new session.</p>
 */
@SuppressWarnings("restriction")
class LoginHandler implements HttpHandler {
    private final UserRegistry userRegistry;
    private final SessionRegistry sessionRegistry;

    public LoginHandler(UserRegistry userRegistry, SessionRegistry sessionRegistry) {
        this.userRegistry = userRegistry;
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        int userId = HttpExchangeUtils.getIntPathParameter(exchange);
        User user = userRegistry.getOrCreateUser(userId);
        String sessionId = sessionRegistry.registerUserSession(user);
        HttpExchangeUtils.writeResponse(exchange, 200, sessionId);
    }

}