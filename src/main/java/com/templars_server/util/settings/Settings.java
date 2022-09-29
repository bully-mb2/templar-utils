package com.templars_server.util.settings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.Map;

public class Settings {

    private static final Logger LOG = LoggerFactory.getLogger(Settings.class);

    private Properties properties;

    public Settings() {
        this.properties = new Properties();
    }

    /**
     *
     * Loads a properties file relative to the executable.
     * If no properties file can be found it will try to create one
     * from a template that should be located in the classpath resources.
     *
     * @param pathname path to settings file
     * @throws IOException if the template file could not be read
     */
    public void load(String pathname) throws IOException {
        LOG.info("Loading config");
        properties = new Properties();
        File file = new File(pathname);
        if (file.exists()) {
            try (FileInputStream stream = new FileInputStream(file)) {
                properties.load(stream);
                appendMissing(pathname);
            }
        } else {
            LOG.info("No config found, creating from default");
            loadDefaults(pathname);
            store(pathname);
        }
    }

    /**
     *
     * Stores the properties to a file
     *
     * @param pathname path to properties file
     * @throws IOException if it can't create the file
     */
    public void store(String pathname) throws IOException {
        LOG.info("Storing properties");
        try (FileOutputStream stream = new FileOutputStream(pathname)) {
            properties.store(stream, null);
        }
    }

    /**
     *
     * Loads the default properties file template from classpath resources
     *
     * @param pathname path to settings file
     * @throws IOException if the template file could not be read
     */
    public void loadDefaults(String pathname) throws IOException {
        LOG.info("Loading default config");
        Properties defaults = new Properties();
        try (InputStream stream = Settings.class.getResourceAsStream("/" + pathname)) {
            defaults.load(stream);
            properties.putAll(defaults);
        }
    }

    /**
     *
     * Appends any missing properties to the properties and the file on disk
     *
     * @throws IOException if it can't locate the file
     */
    private void appendMissing(String pathname) throws IOException {
        LOG.info("Appending missing properties");
        int appended = 0;
        Properties defaults = new Properties();
        try (
             InputStream stream = Settings.class.getResourceAsStream("/" + pathname);
             FileWriter fw = new FileWriter(pathname, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)
        ) {
            defaults.load(stream);
            for (Map.Entry<Object, Object> entry : defaults.entrySet()) {
                Object key = properties.putIfAbsent(entry.getKey(), entry.getValue());
                if (key == null) {
                    out.println(String.format("%s=%s", entry.getKey(), entry.getValue()));
                    appended++;
                }
            }
        }

        LOG.info("Stored " + appended + " new properties");
    }

    public void set(String key, String value) {
        properties.setProperty(key, value);
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

    public InetSocketAddress getAddress(String key) throws MissingPropertyException, InvalidPropertyException {
        String value = get(key);
        String[] split = value.split(":");

        try {
            return new InetSocketAddress(split[0], Integer.parseInt(split[1]));
        } catch (NumberFormatException e) {
            throw new InvalidPropertyException(String.format("Couldn't make address for (%s=%s) expected (%s=host:port)", key, value, key), e);
        }
    }

    @Override
    public String toString() {
        return "Settings{properties=" + properties + "}";
    }
}
