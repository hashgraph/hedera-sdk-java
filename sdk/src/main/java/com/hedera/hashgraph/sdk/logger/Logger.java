package com.hedera.hashgraph.sdk.logger;

import org.slf4j.LoggerFactory;

/**
 *
 */
public class Logger {
    private org.slf4j.Logger logger;
    private LogLevel currentLevel;
    private LogLevel previousLevel;

    /**
     * Constructor
     *
     * @param level the current log level
     */
    public Logger(LogLevel level) {
        logger = LoggerFactory.getLogger(getClass());
        this.currentLevel = level;
        this.previousLevel = level;
    }

    /**
     * Set logger
     *
     * @param logger the new logger
     * @return {@code this}
     */
    public Logger setLogger(org.slf4j.Logger logger) {
        this.logger = logger;
        return this;
    }

    public LogLevel getLevel() {
        return currentLevel;
    }

    /**
     * Set log level
     *
     * @param level the new level
     * @return {@code this}
     */
    public Logger setLevel(LogLevel level) {
        this.previousLevel = this.currentLevel;
        this.currentLevel = level;
        return this;
    }

    /**
     * Set silent mode on/off. If set to true, the logger will not display any log messages. This can also be achieved
     * by calling .setLevel(LogLevel.Silent)`
     *
     * @param silent should the logger be silent
     * @return {@code this}
     */
    public Logger setSilent(boolean silent) {
        if (silent) {
            this.currentLevel = LogLevel.SILENT;
        } else {
            this.currentLevel = this.previousLevel;
        }
        return this;
    }

    /**
     * Log trace
     *
     * @param message   the message to be logged
     * @param arguments the log arguments
     */
    public void trace(String message, Object... arguments) {
        if (isEnabledForLevel(LogLevel.TRACE)) {
            this.logger.trace(message, arguments);
        }
    }

    /**
     * Log debug
     *
     * @param message   the message to be logged
     * @param arguments the log arguments
     */
    public void debug(String message, Object... arguments) {
        if (isEnabledForLevel(LogLevel.DEBUG)) {
            this.logger.debug(message, arguments);
        }
    }

    /**
     * Log info
     *
     * @param message   the message to be logged
     * @param arguments the log arguments
     */
    public void info(String message, Object... arguments) {
        if (isEnabledForLevel(LogLevel.INFO)) {
            this.logger.info(message, arguments);
        }
    }

    /**
     * Log warn
     *
     * @param message   the message to be logged
     * @param arguments the log arguments
     */
    public void warn(String message, Object... arguments) {
        if (isEnabledForLevel(LogLevel.WARN)) {
            this.logger.warn(message, arguments);
        }
    }

    /**
     * Log error
     *
     * @param message   the message to be logged
     * @param arguments the log arguments
     */
    public void error(String message, Object... arguments) {
        if (isEnabledForLevel(LogLevel.ERROR)) {
            this.logger.error(message, arguments);
        }
    }

    /**
     * Returns whether this Logger is enabled for a given {@link LogLevel}.
     *
     * @param level the log level
     * @return true if enabled, false otherwise.
     */
    public boolean isEnabledForLevel(LogLevel level) {
        return level.toInt() >= currentLevel.toInt();
    }
}
