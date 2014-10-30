package com.sbernatsky.tests.king.server;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import com.sbernatsky.tests.king.server.core.Level;
import com.sbernatsky.tests.king.server.core.LevelRegistry;
import com.sbernatsky.tests.king.server.core.LevelScore;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/** Handler which returns top scores for the level. */
@SuppressWarnings("restriction")
class ListLevelScoresHandler implements HttpHandler {
    private final LevelRegistry levelRegistry;

    public ListLevelScoresHandler(LevelRegistry levelRegistry) {
        this.levelRegistry = levelRegistry;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        int levelId = HttpExchangeUtils.getIntPathParameter(exchange);
        Level level = levelRegistry.getLevel(levelId);
        List<LevelScore> scores;
        if (level == null) {
            scores = Collections.emptyList();
        } else {
            scores = level.getScores();
        }

        StringBuilder buf = new StringBuilder((scores.size() + 1) * 16);
        for (LevelScore score : scores) {
            buf.append(score.getUserId()).append('=').append(score.getScore()).append(',');
        }
        if (!scores.isEmpty()) {
            buf.deleteCharAt(buf.length() - 1);
        }

//        if (buf.capacity() > ((scores.size() + 1) * 16)) {
//            com.sbernatsky.tests.king.server.core.Log.log("capacity was changed ({}): expected {}; actual {}",
//                                                          scores.size(), (scores.size() + 1) * 16, buf.capacity());
//        }

        HttpExchangeUtils.writeResponse(exchange, 200, buf);
    }

}