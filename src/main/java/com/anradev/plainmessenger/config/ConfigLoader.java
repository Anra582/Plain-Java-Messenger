package com.anradev.plainmessenger.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Aleksei Zhvakin
 */
public final class ConfigLoader {

    private static final String configFileName = "config.properties";
    private static final Properties properties;

    static {
        properties = new Properties();
        try (InputStream inputStream = ConfigLoader.class.getClassLoader().getResourceAsStream(configFileName)) {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NullPointerException e) {
            System.out.println("Cannot load properties from " + configFileName + ". File not found!");
            throw new RuntimeException(e);
        }
    }

    public static String getValueOfProperty(String propertyName) {
        return properties.getProperty(propertyName);
    }
}