package com.sbernatsky.tests.king.server;

import java.io.IOException;

import com.sbernatsky.tests.king.server.core.Level;

import com.sbernatsky.tests.king.server.core.LevelRegistry;
import com.sbernatsky.tests.king.server.core.User;
import com.sbernatsky.tests.king.server.core.UserContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * Handler which publishes new level score.
 * <p>Loads authenticated user using {@linkplain UserContext#getCurrentUser()} method.</p>
 */
@SuppressWarnings("restriction")
class PublishScoreHandler implements HttpHandler {
    private static final byte[] EMPTY = new byte[0];
    private final LevelRegistry levelRegistry;

    public PublishScoreHandler(LevelRegistry levelRegistry) {
        this.levelRegistry = levelRegistry;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        int levelId = HttpExchangeUtils.getIntPathParameter(exchange);
        Level level = levelRegistry.getOrCreateLevel(levelId);
        User user = UserContext.getCurrentUser();
        String score = HttpExchangeUtils.readRequestBody(exchange);
        int scoreValue = Integer.parseInt(score);
        level.addScore(user, scoreValue);
        HttpExchangeUtils.writeResponse(exchange, 200, EMPTY);
    }

}