package com.sbernatsky.tests.king.client;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

public class Client {
    private static final String HOST = "http://localhost:8000/";

    public static void main(String[] args) throws Exception {
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
        connManager.setMaxTotal(255);
        connManager.setDefaultMaxPerRoute(255);
        CloseableHttpClient client = HttpClientBuilder.create().setConnectionManager(connManager).build();

        runTests(client, 32);

        client.close();
    }

    private static void runTests(final HttpClient client, int threads) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(threads);
        for (int i = 0; i < threads; i++) {
            new Thread(new Runnable() {
                
                @Override
                public void run() {
                    try {
                        for (int i = 0; i < 1024*1024; i++) {
                            try {
                                int userId = getUserId();
                                String session = sessions.get(userId);
                                try {
                                    runTest(client, session);
                                } catch (UnsupportedOperationException e) {
                                    System.out.println("reconnecting: " + userId);
                                    session = login(client, userId);
                                    sessions.put(userId, session);
                                }
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            if ((i+1) % (256 * 10) == 0) {
                                System.out.println('.');
                            } else if ((i+1) % (256) == 0) {
                                System.out.print('.');
                            }
                        }
                    } finally {
                        latch.countDown();
                    }
                }
            }).start();
        }
        
        latch.await();
    }

    private static final int[] USERS;
    private static final AtomicInteger userCounter = new AtomicInteger();
    private static final ConcurrentMap<Integer, String> sessions = new ConcurrentHashMap<>();
    private static final int[] LEVELS;
    private static final AtomicInteger levelCounter = new AtomicInteger();
    static {
        USERS = new int[1024];
        for (int i = 0; i < USERS.length; i++) {
            USERS[i] = Math.abs((int) (Math.random() * (Integer.MAX_VALUE - 1)));
        }

        LEVELS = new int[128];
        for (int i = 0; i < LEVELS.length; i++) {
            LEVELS[i] = Math.abs((int) (Math.random() * 128));
        }
    }
    private static int getUserId() {
        return USERS[userCounter.getAndIncrement() % USERS.length];
    }
    private static int getLevelId() {
        return LEVELS[levelCounter.getAndIncrement() % LEVELS.length];
    }
    private static int getScore() {
        return Math.abs((int) (Math.random() * 32));
    }

    private static void runTest(HttpClient client, String session) throws IOException {
        if (session == null) {
            throw new UnsupportedOperationException();
        }

        for (int i = 0; i < 64; i++) {
            int levelId = getLevelId();
            publishScore(client, session, levelId, getScore());
            String scores = listScore(client, levelId);
//            System.out.println("scores (" + levelId + "): " + scores);
            scores = listScore(client, 255);
//            System.out.println("scores (" + 255 + "): " + scores);
        }
    }

    private static String listScore(HttpClient client, int level) throws IOException {
        HttpGet request = new HttpGet(HOST + level + "/highscorelist");
        return client.execute(request, new ResponseHandler<String>() {
            @Override
            public String handleResponse(HttpResponse response) throws IOException {
                int status = response.getStatusLine().getStatusCode();
                if (status == 403) {
                    throw new UnsupportedOperationException();
                } else if (status == 200) {
                    HttpEntity entity = response.getEntity();
                    return entity != null ? EntityUtils.toString(entity) : null;
                } else {
                    throw new RuntimeException("Unexpected response status: " + status);
                }
            }
        });
    }

    private static void publishScore(HttpClient client, String session, int level, int score) throws IOException {
        HttpPost request = new HttpPost(HOST + level + "/score?sessionkey=" + session);
        request.setEntity(new ByteArrayEntity(String.valueOf(score).getBytes()));
        client.execute(request, new ResponseHandler<Void>() {
            @Override
            public Void handleResponse(HttpResponse response) throws IOException {
                int status = response.getStatusLine().getStatusCode();
                if (status == 403) {
                    throw new UnsupportedOperationException();
                } else if (status != 200) {
                    throw new RuntimeException("Unexpected response status: " + status);
                }
                return null;
            }
        });
    }

    private static String login(HttpClient client, int userId) throws IOException {
        HttpGet request = new HttpGet(HOST + userId + "/login");
        return client.execute(request, new ResponseHandler<String>() {
            @Override
            public String handleResponse(HttpResponse response) throws IOException {
                int status = response.getStatusLine().getStatusCode();
                if (status == 403) {
                    throw new UnsupportedOperationException();
                } else if (status == 200) {
                    HttpEntity entity = response.getEntity();
                    return entity != null ? EntityUtils.toString(entity) : null;
                } else {
                    throw new RuntimeException("Unexpected response status: " + status);
                }
            }
        });
    }
}
