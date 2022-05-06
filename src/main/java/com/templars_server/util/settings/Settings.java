package com.templars_server.util.settings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class Settings {

    private static final Logger LOG = LoggerFactory.getLogger(Settings.class);

    private Properties properties;

    /**
     *
     * Loads a config file relative to the executable.
     * If no config can be found it will try to create one
     * from a template that should be located in the classpath resources.
     *
     * @param pathname path to settings file
     * @throws IOException if the template file could not be located
     */
    public void load(String pathname) throws IOException {
        properties = new Properties();
        LOG.info("Loading config");
        File file = new File(pathname);
        if (file.exists()) {
            try (FileInputStream stream = new FileInputStream(file)) {
                properties.load(stream);
            }
        } else {
            try (FileOutputStream stream = new FileOutputStream(file)) {
                LOG.info("No config found, creating from default");
                properties.load(Settings.class.getResourceAsStream("/" + pathname));
                properties.store(stream, null);
            }
        }
    }

    public String get(String key) throws MissingPropertyException {
        String value = properties.getProperty(key);
        if (value == null) {
            throw new MissingPropertyException("No value found for key " + key);
        }

        return value;
    }

    public int getInt(String key) throws MissingPropertyException, InvalidPropertyException {
        String value = get(key);

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new InvalidPropertyException(String.format("Couldn't parse (%s=%s) to int", key, value), e);
        }
    }

    public boolean getBoolean(String key) throws MissingPropertyException, InvalidPropertyException {
        String value = get(key);

        if (value.equals("true")) {
            return true;
        } else if (value.equals("false")) {
            return false;
        }

        throw new InvalidPropertyException(String.format("Couldn't parse (%s=%s) to boolean", key, value));
    }

    public SocketAddress getAddress(String key) throws MissingPropertyException, InvalidPropertyException {
        String value = get(key);
        String[] split = value.split(":");

        try {
            return new InetSocketAddress(split[0], Integer.parseInt(split[0]));
        } catch (NumberFormatException e) {
            throw new InvalidPropertyException(String.format("Couldn't make address for (%s=%s) expected (%s=host:port)", key, value, key), e);
        }
    }

    @Override
    public String toString() {
        return "Settings{properties=" + properties + "}";
    }
}
