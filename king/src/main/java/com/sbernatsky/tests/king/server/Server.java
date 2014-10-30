package com.sbernatsky.tests.king.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.sbernatsky.tests.king.server.core.Log;

import com.sbernatsky.tests.king.server.core.Session;
import com.sbernatsky.tests.king.server.core.SessionRegistry;
import com.sbernatsky.tests.king.server.core.UserContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/** Internal server to serve http requests. Uses {@linkplain HttpServer} internally. */
@SuppressWarnings("restriction")
public class Server {

    private HttpServer server;
    private final Router router = new Router(new DefaultHttpHandler());
    private int threadCount = Runtime.getRuntime().availableProcessors();
    private SessionRegistry sessionRegistry;

    public void start(InetSocketAddress addr) throws IOException {
        server = HttpServer.create(addr, 0);
        server.setExecutor(Executors.newFixedThreadPool(threadCount));
        server.createContext("/", router);
        server.start();
    }

    public void shutdown() throws InterruptedException {
        server.stop(0);
        ExecutorService executor = (ExecutorService)server.getExecutor();
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.SECONDS);
    }

    /** Adds handler to serve un-authenticated requests. */
    public void addPublicHandler(String pathSuffix, HttpHandler handler) {
        router.addHandler(pathSuffix, handler);
    }

    /** Adds handler to serve authenticated requests (those which have 'sessionkey' parameter in request). */
    public void addAuthenticatedHandler(String pathSuffix, HttpHandler handler) {
        HttpHandler authenticated = new AuthenticatedHandler(handler, sessionRegistry);
        router.addHandler(pathSuffix, authenticated);
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }
    public void setSessionRegistry(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    /**
     * Performs request routing according to the internal map of uri suffixes to the handlers.
     * <p>In case when handler is not found processing is done using default handler.</p>
     */
    static class Router implements HttpHandler {
        private final HttpHandler defaultHandler;
        private final Map<String, HttpHandler> handlers = new HashMap<>();

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                HttpHandler handler = null;
                String p = HttpExchangeUtils.getPathSuffix(exchange);
                handler = handlers.get(p);
                if (handler == null) {
                    handler = defaultHandler;
                }
                handler.handle(exchange);
            } catch (Exception e) {
                Log.error("processing failed for uri: " + exchange.getRequestURI(), e);
                HttpExchangeUtils.writeResponse(exchange, 500, e.getClass() + ": " + e.getMessage());
            } finally {
                exchange.close();
            }
        }

        public void addHandler(String pathSuffix, HttpHandler handler) {
            handlers.put(pathSuffix, handler);
        }

        public Router(HttpHandler defaultHandler) {
            this.defaultHandler = defaultHandler;
        }
    }

    /**
     * Router delegate which performs authentication and then delegates to the parent router.
     * <p>Authenticated user is stored in {@linkplain UserContext} for the usage in delegate.</p>
     */
    static class AuthenticatedHandler implements HttpHandler {
        private final HttpHandler delegate;
        private final SessionRegistry sessionRegistry;

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String sessionId = HttpExchangeUtils.getRequestParameter(exchange, "sessionkey");
            Session session = sessionRegistry.getSession(sessionId);
            if (session == null || session.isExpired(now())) {
                HttpExchangeUtils.writeForbiddenResponse(exchange);
                return;
            }
            try {
                UserContext.setCurrentUser(session.getUser());
                delegate.handle(exchange);
            } finally {
                UserContext.clear();
            }
        }

        protected long now() {
            return System.currentTimeMillis();
        }

        public AuthenticatedHandler(HttpHandler delegate, SessionRegistry sessionRegistry) {
            this.delegate = delegate;
            this.sessionRegistry = sessionRegistry;
        }
    }

    /** Router which performs default action (returns '404 Not Found' error). */
    static class DefaultHttpHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Log.log("request: {};{}",
                    exchange.getRequestMethod(),
                    exchange.getRequestURI());
            HttpExchangeUtils.writeNotFoundResponse(exchange);
        }
    }

}
