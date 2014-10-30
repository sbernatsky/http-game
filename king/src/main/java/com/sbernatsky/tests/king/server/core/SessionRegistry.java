package com.sbernatsky.tests.king.server.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Usually sessions are stored somewhere and are loaded through some service.
 * <p>
 * Current implementation keeps all sessions in local map and uses separate thread to clean it from expired sessions.
 * </p>
 * <p>
 * Currens implementation uses {@linkplain ThreadLocalRandom#nextInt()} to generate unique session id. It may provide
 * collisions and collision detection logic is not implemented here
 * </p>
 */
public class SessionRegistry {
    private final ConcurrentMap<String, Session> sessions = new ConcurrentHashMap<>();
    private Cleaner cleaner;
    private final long sessionValidityPeriod;

    public SessionRegistry(long sessionValidityPeriod) {
        this.sessionValidityPeriod = sessionValidityPeriod;
    }

    public Session getSession(String sessionId) {
        return sessions.get(sessionId);
    }

    public String registerUserSession(User user) {
        String id = Integer.toHexString(ThreadLocalRandom.current().nextInt());
        Session session = new Session(user, now() + sessionValidityPeriod);
        sessions.put(id, session);
        return id;
    }

    protected long now() {
        return System.currentTimeMillis();
    }

    public void start() {
        cleaner = new Cleaner();
        Thread cleanupThread = new Thread(new Cleaner(), "session registry cleaner");
        cleanupThread.setDaemon(true);
        cleanupThread.start();
    }

    private class Cleaner implements Runnable {
        private volatile boolean running = true;

        @Override
        public void run() {
            try {
                while (running) {
                    Thread.sleep(1000L * 30L);

                    List<String> expiredSessions = new ArrayList<>(1024);
                    long now = now();
                    for (Entry<String, Session> entry : sessions.entrySet()) {
                        if (entry.getValue().isExpired(now)) {
                            expiredSessions.add(entry.getKey());
                        }
                    }
                    Log.log("cleaning expired sessions: {}", expiredSessions.size());
                    for (String expired : expiredSessions) {
                        sessions.remove(expired);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        public void shutdown() {
            running = false;
        }
    }

    public void shutdown() {
        if (cleaner != null) {
            cleaner.shutdown();
        }
    }
}
