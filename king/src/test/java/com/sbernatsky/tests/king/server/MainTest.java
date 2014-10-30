package com.sbernatsky.tests.king.server;



import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.sbernatsky.tests.king.server.core.Log;

import com.sbernatsky.tests.king.server.Server.AuthenticatedHandler;
import com.sbernatsky.tests.king.server.Server.DefaultHttpHandler;
import com.sbernatsky.tests.king.server.Server.Router;
import com.sbernatsky.tests.king.server.core.LevelRegistry;
import com.sbernatsky.tests.king.server.core.SessionRegistry;
import com.sbernatsky.tests.king.server.core.UserRegistry;

public class MainTest {
//    private static final int[] USERS;
//    private static final int[] LEVELS;
    private static final URI[] LOGINS;
    private static final URI[] LIST_SCORE_URLS;
    private static final String[] PUBLISH_URLS;
    private static final NullOut NULL_OUT = new NullOut();
    private static final int READS_COUNT = 1024 * 1024 * 1024;
    private static final int WRITES_COUNT = 1024 * 1024 * 1024;

    static {
        int USERS = 1024;
        LOGINS = new URI[USERS];
        for (int i = 0; i < USERS; i++) {
            int value = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE - 1);
            LOGINS[i] = URI.create(String.format("/%d/login", value));
        }

        int LEVELS = 2;
        LIST_SCORE_URLS = new URI[LEVELS];
        PUBLISH_URLS = new String[LEVELS];
        for (int i = 0; i < LEVELS; i++) {
            int value = ThreadLocalRandom.current().nextInt(128);
            LIST_SCORE_URLS[i] = URI.create(String.format("/%d/highscorelist", value));
            PUBLISH_URLS[i] = String.format("/%d/score?sessionkey=", value);
        }
    }

    public static void main(String[] args) throws Exception {
        SessionRegistry sessionRegistry = new SessionRegistry(1000L * 60L); // FIXME
        LevelRegistry levelRegistry = new LevelRegistry();
        UserRegistry userRegistry = new UserRegistry();

        final Router router = new Router(new DefaultHttpHandler());
        router.addHandler("score", new AuthenticatedHandler(new PublishScoreHandler(levelRegistry), sessionRegistry));
        router.addHandler("login", new LoginHandler(userRegistry, sessionRegistry));
        router.addHandler("highscorelist", new ListLevelScoresHandler(levelRegistry));

        sessionRegistry.start();

        int readTasksCount = 5;
        int writeTasksCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(readTasksCount + writeTasksCount);
        for (int i = 0; i < readTasksCount; i++) {
            executor.execute(new ReadTask(router));
        }
        for (int i = 0; i < writeTasksCount; i++) {
            executor.execute(new WriteTask(router));
        }

        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        sessionRegistry.shutdown();
    }

    private static class WriteTask implements Runnable {
        private final Router router;
        private final AtomicInteger levelCounter;
        private final AtomicInteger userCounter;
        private final Random score;
        private final URI[] publishUrls;

        private WriteTask(Router router) {
            this.router = router;
            this.levelCounter = new AtomicInteger(ThreadLocalRandom.current().nextInt(PUBLISH_URLS.length));
            this.userCounter = new AtomicInteger(ThreadLocalRandom.current().nextInt(LOGINS.length));
            this.score = new Random(ThreadLocalRandom.current().nextLong());
            this.publishUrls = new URI[PUBLISH_URLS.length];
        }

        private void initPublishUrls(String session) {
            for (int i = 0; i < publishUrls.length; i++) {
                publishUrls[i] = URI.create(PUBLISH_URLS[i] + session);
            }
        }
        private URI getPublishUrl() {
            return publishUrls[levelCounter.getAndIncrement() % publishUrls.length];
        }
        private URI getUserUrl() {
            return LOGINS[userCounter.getAndIncrement() % LOGINS.length];
        }
        private int getScore() {
            return score.nextInt(32) + 1;
        }

        @Override
        public void run() {
            BytesOut loginOut = new BytesOut();
            HttpExchangeImpl loginExchange = new HttpExchangeImpl();
            loginExchange.setResponseBody(loginOut);

            HttpExchangeImpl publishExchange = new HttpExchangeImpl();
            publishExchange.setResponseBody(NULL_OUT);
            try {
                long start = System.currentTimeMillis();
                for (int i = 0; i < (WRITES_COUNT / 100); i++) {
                    loginExchange.setRequestURI(getUserUrl());
                    router.handle(loginExchange);
                    if (loginExchange.getResponseCode() != 200) {
                        System.err.println("FAILED");
                        throw new RuntimeException();
                    }

                    String session = loginOut.getSession();
                    initPublishUrls(session);
                    for (int j = 0; j < 100; j++) {
                        int score = getScore();
                        publishExchange.setRequestURI(getPublishUrl());
                        publishExchange.setRequestBody(new ByteArrayInputStream(String.valueOf(score).getBytes()));
                        router.handle(publishExchange);
                        if (publishExchange.getResponseCode() != 200) {
                            System.err.println("FAILED");
                            break;
                        }
                    }

//                    if ((i * 100 + 500000) % 1000000 == 0) {
//                        long end = System.currentTimeMillis();
//                        Log.log("publish({}): {} ms", i * 100, (end - start));
//                        start = end;
//                    }
                }
            } catch (IOException e) {
                //
            }
        }

    }

    private static class ReadTask implements Runnable {
        private final Router router;
        private final AtomicInteger levelCounter;

        private ReadTask(Router router) {
            this.router = router;
            this.levelCounter = new AtomicInteger(ThreadLocalRandom.current().nextInt(LIST_SCORE_URLS.length));
        }

        private URI getLevelUrl() {
            return LIST_SCORE_URLS[levelCounter.getAndIncrement() % LIST_SCORE_URLS.length];
        }

        @Override
        public void run() {
            HttpExchangeImpl exchange = new HttpExchangeImpl();
            exchange.setResponseBody(NULL_OUT);
            try {
                long start = System.currentTimeMillis();
                for (int i = 0; i < READS_COUNT; i++) {
                    URI uri = getLevelUrl();
                    exchange.setRequestURI(uri);
                    router.handle(exchange);
                    if (exchange.getResponseCode() != 200) {
                        System.err.println("FAILED");
                        throw new RuntimeException();
                    }

//                    if ((i + 500000) % 1000000 == 0) {
//                        long end = System.currentTimeMillis();
//                        Log.log("    get({}): {} ms", i, (end - start));
//                        start = end;
//                    }
                }
            } catch (IOException e) {
                //
            }
        }

    }
}
