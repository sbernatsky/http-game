package com.sbernatsky.tests.king.server;

import java.net.InetSocketAddress;

import com.sbernatsky.tests.king.server.core.LevelRegistry;
import com.sbernatsky.tests.king.server.core.Log;
import com.sbernatsky.tests.king.server.core.SessionRegistry;
import com.sbernatsky.tests.king.server.core.UserRegistry;

/** Main class which creates and initializes server. */
public class Main {
    private static final long DEFAULT_SESSION_VALID_PERIOD = 1000L * 60L * 10L;

    public static void main(String[] args) throws Exception {
        SessionRegistry sessionRegistry = new SessionRegistry(DEFAULT_SESSION_VALID_PERIOD);
        LevelRegistry levelRegistry = new LevelRegistry();
        UserRegistry userRegistry = new UserRegistry();

        Server server = new Server();
        server.setSessionRegistry(sessionRegistry);
        //server.setThreadCount(2); using all available cores
        server.addAuthenticatedHandler("score", new PublishScoreHandler(levelRegistry));
        server.addPublicHandler("login", new LoginHandler(userRegistry, sessionRegistry));
        server.addPublicHandler("highscorelist", new ListLevelScoresHandler(levelRegistry));

        sessionRegistry.start();
        server.start(new InetSocketAddress(8000));
        Log.log("server started. press enter to shutdown.");
        
        System.in.read();
        server.shutdown();
        sessionRegistry.shutdown();
    }
}
