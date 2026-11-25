package com.kc1vmz.netlog;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.status.Status;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

class LoggingConfigurationTest {

    @TempDir
    Path directory;

    private LoggerContext configure(Path destination, String configuration) throws Exception {
        LoggerContext context = new LoggerContext();
        context.setName("netlog-logging-test");
        context.setMDCAdapter(new ch.qos.logback.classic.util.LogbackMDCAdapter());
        context.putProperty("java.io.tmpdir", directory.toString());
        context.putProperty("NETLOG_TEMP_DIR", destination.toString());
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(context);
        configurator.doConfigure(new ByteArrayInputStream(configuration.getBytes(StandardCharsets.UTF_8)));
        return context;
    }

    private String configuration() throws Exception {
        try (var input = getClass().getResourceAsStream("/logback.xml")) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Test
    void createsDirectoryWritesPlainTextExceptionsAndAppendsAfterRestart() throws Exception {
        Path destination = directory.resolve("logs with spaces");
        for (int run = 0; run < 2; run++) {
            LoggerContext context = configure(destination, configuration());
            try {
                Logger logger = context.getLogger("netlog.test");
                logger.debug("hidden debug message");
                logger.info("startup {}", run);
                logger.error("example failure", new IllegalStateException("example cause"));
            } finally {
                context.stop();
            }
        }
        String log = Files.readString(destination.resolve("netlog.log"));
        assertThat(log).contains("startup 0", "startup 1", "INFO", "ERROR",
                "java.lang.IllegalStateException: example cause", "[main]")
                .doesNotContain("hidden debug message", "\u001b[");
        assertThat(log).containsPattern("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3}");
    }

    @Test
    void unsetDirectoryFallsBackToNetlogUnderJavaTemporaryDirectory() throws Exception {
        String configuration = configuration().replace("NETLOG_TEMP_DIR:-",
                "NETLOG_TEST_UNSET_" + java.util.UUID.randomUUID().toString().replace("-", "") + ":-");
        LoggerContext context = configure(directory.resolve("unused"), configuration);
        try {
            context.getLogger("netlog.test").info("fallback destination");
        } finally {
            context.stop();
        }
        assertThat(Files.readString(directory.resolve("netlog/netlog.log"))).contains("fallback destination");
    }

    @Test
    void rollsAndCompressesLogsWhenSizeLimitIsReached() throws Exception {
        Path destination = directory.resolve("rolling");
        LoggerContext context = configure(destination, configuration().replace("10MB", "1KB"));
        try {
            Logger logger = context.getLogger("netlog.test");
            for (int index = 0; index < 30; index++) {
                logger.info("rolling record {} {}", index, "x".repeat(300));
            }
        } finally {
            context.stop();
        }
        try (var files = Files.list(destination)) {
            var archives = files.filter(file -> file.getFileName().toString().endsWith(".log.gz")).toList();
            assertThat(archives).isNotEmpty();
            try (var input = new GZIPInputStream(Files.newInputStream(archives.get(0)))) {
                assertThat(new String(input.readAllBytes(), StandardCharsets.UTF_8)).contains("rolling record");
            }
        }
        assertThat(Files.readString(destination.resolve("netlog.log"))).contains("rolling record 29");
    }

    @Test
    @SuppressWarnings("unchecked")
    void invalidDirectoryKeepsConsoleLoggingAvailableWithoutFatalLogbackErrors() throws Exception {
        Path file = directory.resolve("not-a-directory");
        Files.writeString(file, "existing file");
        LoggerContext context = configure(file.resolve("logs"), configuration());
        try {
            Logger root = context.getLogger(Logger.ROOT_LOGGER_NAME);
            ConsoleAppender<ILoggingEvent> console = (ConsoleAppender<ILoggingEvent>) root.getAppender("STDOUT");
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            console.setOutputStream(output);
            context.getLogger("netlog.test").info("console still works");
            assertThat(output.toString(StandardCharsets.UTF_8)).contains("console still works");
            assertThat(root.getAppender("FILE").isStarted()).isFalse();
            assertThat(context.getStatusManager().getCopyOfStatusList())
                    .noneMatch(status -> status.getLevel() == Status.ERROR)
                    .anyMatch(status -> status.getMessage().contains("Console logging remains enabled"));
        } finally {
            context.stop();
        }
    }
}
