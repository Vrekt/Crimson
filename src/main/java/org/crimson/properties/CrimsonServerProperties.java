package org.crimson.properties;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents memory copy of the "server.properties" file.
 */
@Log4j2
public final class CrimsonServerProperties implements ServerPropertiesConfiguration {

    /**
     * Map of properties.
     */
    private final Map<String, String> properties = new HashMap<>();

    @Override
    public boolean generateServerPropertiesIfNeeded(Path where) {
        if (Files.exists(where)) return true;

        try {
            log.info("Generating server.properties file...");
            Files.createFile(where);

            // populate map with default values.
            for (DefaultServerProperties property : DefaultServerProperties.values()) {
                properties.put(property.name, property.value);
            }

            // write all this to the file.
            final List<String> propertyWithValues = new ArrayList<>();
            properties.forEach((property, value) -> propertyWithValues.add(property + "=" + value));
            FileUtils.writeLines(where.toFile(), propertyWithValues);
        } catch (IOException exception) {
            log.error("Failed to generate server.properties file", exception);
            return false;
        }

        return true;
    }

    @Override
    public boolean loadServerProperties(Path where) {
        if (!properties.isEmpty()) return true;

        try {
            log.info("Reading server.properties file...");
            FileUtils.readLines(where.toFile(), StandardCharsets.UTF_8)
                    .forEach(line -> {
                        final String propertyName = StringUtils.substringBefore(line, "=");
                        final String propertyValue = StringUtils.substringAfter(line, "=");
                        properties.put(propertyName, propertyValue);
                    });
        } catch (IOException exception) {
            log.error("Failed to load server.properties file", exception);
            return false;
        }
        return true;
    }

    /**
     * Get a property.
     *
     * @param name the name
     * @return the property
     */
    @Override
    public String getPropertyAsString(String name) {
        return properties.get(name);
    }

    /**
     * Get a property.
     *
     * @param name the name
     * @return the property
     */
    @Override
    public int getPropertyAsInteger(String name) {
        return NumberUtils.createInteger(properties.get(name));
    }

    /**
     * Get a property.
     *
     * @param name the name
     * @return the property
     */
    @Override
    public boolean getPropertyAsBoolean(String name) {
        return Boolean.parseBoolean(properties.get(name));
    }
}
