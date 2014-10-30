package com.sbernatsky.tests.king.server;

import static org.junit.Assert.assertEquals;

import java.net.URI;

import static org.mockito.Mockito.when;

import com.sun.net.httpserver.HttpExchange;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@SuppressWarnings("restriction")
@RunWith(MockitoJUnitRunner.class)
public class HttpExchangeUtilsTest {
    @Mock private HttpExchange exchange;

    @Before
    public void setUp() {
        when(exchange.getRequestURI()).thenReturn(URI.create("/123/b/c"));
    }

    @Test
    public void testGetPathSuffix() {
        assertEquals("c", HttpExchangeUtils.getPathSuffix(exchange));
    }

    @Test
    public void testGetIntPathParameter() throws Exception {
        assertEquals(123, HttpExchangeUtils.getIntPathParameter(exchange));
    }

}
