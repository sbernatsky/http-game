package com.sbernatsky.tests.king.server;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;

@SuppressWarnings("restriction")
class HttpExchangeImpl extends HttpExchange {

    private URI uri;
    private int responseCode;
    private OutputStream responseBody;
    private InputStream requestBody;

    @Override public void close() { }

    @Override public int getResponseCode() {
        return responseCode;
    }
    @Override
    public InputStream getRequestBody() {
        return requestBody;
    }

    @Override
    public String getRequestMethod() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public URI getRequestURI() {
        return uri;
    }
    
    @Override
    public OutputStream getResponseBody() {
        return responseBody;
    }
    @Override
    public void sendResponseHeaders(int arg0, long arg1) {
        this.responseCode = arg0;
        // TODO Auto-generated method stub
        
    }

    @Override public Object getAttribute(String arg0) { throw new UnsupportedOperationException(); }
    @Override public HttpContext getHttpContext() { throw new UnsupportedOperationException(); }
    @Override public InetSocketAddress getLocalAddress() {  throw new UnsupportedOperationException(); }
    @Override public HttpPrincipal getPrincipal() { throw new UnsupportedOperationException(); }
    @Override public String getProtocol() {  throw new UnsupportedOperationException(); }
    @Override public InetSocketAddress getRemoteAddress() { throw new UnsupportedOperationException(); }
    @Override public Headers getRequestHeaders() { throw new UnsupportedOperationException(); }
    @Override public Headers getResponseHeaders() { throw new UnsupportedOperationException(); }
    @Override public void setAttribute(String arg0, Object arg1) { throw new UnsupportedOperationException(); }
    @Override public void setStreams(InputStream arg0, OutputStream arg1) { throw new UnsupportedOperationException(); }

    public void setRequestURI(URI uri) {
        this.uri = uri;
    }

    public void setResponseBody(OutputStream out) {
        this.responseBody = out;
    }

    public void setRequestBody(InputStream requestBody) {
        this.requestBody = requestBody;
    }

}