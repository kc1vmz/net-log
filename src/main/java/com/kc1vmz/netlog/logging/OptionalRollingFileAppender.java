package com.kc1vmz.netlog.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.RollingFileAppender;

/**
 * Keeps console logging and application startup available when file logging fails.
 * Spring Boot otherwise treats Logback appender errors as fatal startup errors.
 */
public class OptionalRollingFileAppender extends RollingFileAppender<ILoggingEvent> {

    @Override
    public void addError(String message) {
        reportFileLoggingFailure(message, null);
    }

    @Override
    public void addError(String message, Throwable cause) {
        reportFileLoggingFailure(message, cause);
    }

    private void reportFileLoggingFailure(String message, Throwable cause) {
        String warning = "NETLOG file logging failed. Console logging remains enabled. " + message;
        // Keep the failure visible without recording a fatal Logback error status.
        super.addWarn(warning, cause);
        System.err.println(warning);
        if (cause != null) {
            System.err.println(cause);
        }
    }
}
