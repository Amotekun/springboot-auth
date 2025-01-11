package com.example.auth.util;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class LoggerWrapper {
    private final Logger logger;

    private LoggerWrapper(Class<?> clazz) {
        this.logger = LoggerFactory.getLogger(clazz);
    }

    public static LoggerWrapper getLogger(Class<?> clazz) {
        return new LoggerWrapper(clazz);
    }

    public void logError(String message) {
        logger.error("ERROR: " + message);
    }

    public void logInfo(String message) {
        logger.info("INFO: " + message);
    }

    public void logWarn(String message) {
        logger.warn("WARN: " + message);
    }

    public void logDebug(String message) {
        logger.debug("DEBUG: " + message);
    }
}
