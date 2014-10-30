package com.sbernatsky.tests.king.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.CharBuffer;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;

import com.sun.net.httpserver.HttpExchange;

/** Some utility methods to ease work with {@linkplain HttpExchange}. */
@SuppressWarnings("restriction") 
class HttpExchangeUtils {

    /** Returns parameter value specified in request uri or null if it is not found. */
    public static String getRequestParameter(HttpExchange exchange, String param) {
        String query = exchange.getRequestURI().getQuery();
        String[] paramAndValuePairs = query.split("&");
        for (String paramAndValue : paramAndValuePairs) {
            if (paramAndValue.startsWith(param + "=")) {
                return paramAndValue.substring(param.length() + 1);
            }
        }

        return null;
    }

    /** Returns last segment of the requested uri. Avoids excessive object creation. */
    public static String getPathSuffix(HttpExchange exchange) {
        String path = exchange.getRequestURI().getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }
    /** Returns first segment of the requested uri as integer. Avoids excessive object creation. */
    public static int getIntPathParameter(HttpExchange exchange) {
        String path = exchange.getRequestURI().getPath();
        int start = path.indexOf('/') + 1;
        int end = path.indexOf('/', start);
        return Integer.parseInt(path.substring(start, end));
    }

    private static final byte[] NOT_FOUND = "NOT FOUND".getBytes();
    private static final byte[] FORBIDDEN = "Forbidden".getBytes();

    public static void writeForbiddenResponse(HttpExchange exchange) throws IOException {
        writeResponse(exchange, 403, FORBIDDEN);
    }
    public static void writeNotFoundResponse(HttpExchange exchange) throws IOException {
        writeResponse(exchange, 401, NOT_FOUND);
    }
    public static void writeResponse(HttpExchange exchange, int code, byte[] body) throws IOException {
        exchange.sendResponseHeaders(code, body.length);
        exchange.getResponseBody().write(body);
    }
    // TODO: test perfomance vs toString().getBytes() using jmh (http://stackoverflow.com/a/19472272)
    // TODO: reuse CharsetEncoder
    public static void writeResponse(HttpExchange exchange, int code, CharSequence body) throws IOException {
        CharsetEncoder encoder = StandardCharsets.UTF_8.newEncoder();
        CharBuffer buffer = CharBuffer.wrap(body);
        byte[] data = encoder.encode(buffer).array();
        writeResponse(exchange, code, data);
    }

    public static String readRequestBody(HttpExchange exchange) throws IOException {
        byte[] buf = new byte[32];
        ByteArrayOutputStream out = new ByteArrayOutputStream(32);
        InputStream in = exchange.getRequestBody();
        int count;
        while ((count = in.read(buf)) != -1) {
            out.write(buf, 0 , count);
        }
        return out.toString();
    }
}
