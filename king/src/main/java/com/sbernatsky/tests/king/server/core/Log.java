package com.sbernatsky.tests.king.server.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/** Simple utility class for logging. Usually should be replaced with slf4j. */
public class Log {
    private static final Logger LOG = LoggerFactory.getLogger("LOG");
    public static void log(String format, Object ... args) {
        LOG.info(format, args);
    }
    public static void error(String message, Throwable error) {
        LOG.info(message, error);
    }
}
