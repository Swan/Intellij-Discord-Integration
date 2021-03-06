package com.almightyalpaca.jetbrains.plugins.discord.debug;

import com.almightyalpaca.jetbrains.plugins.discord.components.DiscordIntegrationApplicationComponent;
import com.almightyalpaca.jetbrains.plugins.discord.settings.DiscordIntegrationApplicationSettings;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.application.ApplicationManager;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

public class Debug
{
    @NotNull
    private static final DiscordIntegrationApplicationSettings SETTINGS = DiscordIntegrationApplicationSettings.getInstance();
    @NotNull
    private static final DateTimeFormatter FILE_NAME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss").withZone(ZoneId.systemDefault()).withLocale(Locale.getDefault());
    @NotNull
    private static final String PRODUCT_CODE = ApplicationInfo.getInstance().getBuild().getProductCode();
    private static final long START_TIME = ApplicationManager.getApplication().getStartTime();

    public static boolean isEnabled()
    {
        return SETTINGS.getSettings().isDebugLoggingEnabled();
    }

    public static void log(String name, Level level, String message)
    {
        log(null, name, level, message);
    }

    public static synchronized void log(@Nullable String folder, @NotNull String name, @NotNull Level level, @NotNull String message)
    {
        if (folder != null || SETTINGS.getSettings().isDebugLoggingEnabled())
        {
            try
            {
                Path path = Paths.get(folder == null ? SETTINGS.getSettings().getDebugLogFolder() : folder, PRODUCT_CODE + "-" + FILE_NAME_FORMATTER.format(Instant.ofEpochMilli(START_TIME)) + ".log");

                Files.createDirectories(path.getParent());

                String line = StringUtils.leftPad(name, 100) + ": " + level.getName() + " - " + message + System.lineSeparator();

                Files.write(path, line.getBytes("UTF-8"), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public static void printDebugInfo(@Nullable String folder)
    {
        log("forced debug data dump", Level.TRACE, Objects.toString(DiscordIntegrationApplicationComponent.getInstance().getInstanceInfo()));
    }

    public enum Level
    {
        TRACE("TRACE"),
        DEBUG("DEBUG"),
        WARN("WARN"),
        ERROR("ERROR"),
        INFO("INFO");

        private final String name;

        Level(String name)
        {
            this.name = StringUtils.leftPad(name, 5);
        }

        public String getName()
        {
            return name;
        }
    }
}
