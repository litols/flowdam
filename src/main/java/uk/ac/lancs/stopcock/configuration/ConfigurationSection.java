/*
 * YAML/Memory Configuration Handling - Copyright © 2014 - Peter Wood <peter@alastria.net>
 *
 * Under license to Lancaster University for academic and non commercial use in the Stopcock project from
 * Peter Wood <peter@alastria.net>, any distribution in source or binary form outside Lancaster University
 * is expressly prohibited. Configuration system was written solely outside Lancaster University, all rights
 * are reserved.
 */
package uk.ac.lancs.stopcock.configuration;

import java.util.List;

/**
 * The {@code ConfigurationSection} interface provides a memory data store for configuration data.
 */
public interface ConfigurationSection {
    public static final char PATH_SEPARATOR = '.';
    public static final String PATH_SEPARATOR_REGEX = "\\.";

    /**
     * Get the current path that is represented by this ConfigurationSection.
     *
     * @return the current path, or null if root
     */
    public String getPath();

    /**
     * Get the name of this ConfigurationSection.
     *
     * @return the name of this configuration section
     */
    public String getName();

    /**
     * Get the root configuration section to which this ConfigurationSection belongs.
     *
     * @return the root configuration section
     */
    public ConfigurationSection getRoot();

    /**
     * Get the parent to this configuration section.
     *
     * @return the parent configuration section, or null if root
     */
    public ConfigurationSection getParent();

    /**
     * Get a list of keys from this ConfigurationSection.
     *
     * @param deep if keys should be fetched recursively
     * @return list of relative paths which are held within this ConfigurationSection
     */
    public List<String> getKeys(boolean deep);

    /**
     * Check to see if a path is set.
     *
     * @param path path to check
     * @return true if the path exists, will return false if any part of path does not exist
     */
    public boolean isSet(String path);

    /**
     * Set an object into the ConfigurationSection, note only Strings, Integers, Doubles, Longs, Booleans are accepted.
     *
     * @param path  path to store value at
     * @param value value object to store
     */
    public void set(String path, Object value);

    /**
     * Get an object from the ConfigurationSection.
     *
     * @param path path to get value of
     * @return the object found
     */
    public Object get(String path);

    /**
     * Get a string object from the ConfigurationSection.
     *
     * @param path path to retrieve
     * @return string representation of object, or null if it does not exist
     */
    public String getString(String path);

    /**
     * Get a string object from the ConfigurationSection.
     *
     * @param path         path to retrieve
     * @param defaultValue default value to return
     * @return string representation of object, or the provided default value if it does not exist
     */
    public String getString(String path, String defaultValue);

    /**
     * Check to see if the path being requested is natively represented as a string.
     *
     * @param path path to check
     * @return true if the value is a native string
     */
    public boolean isString(String path);

    /**
     * Get a Integer object from the ConfigurationSection.
     *
     * @param path path to retrieve
     * @return int representation of object, or null if it does not exist
     */
    public int getInteger(String path);

    /**
     * Get a Integer object from the ConfigurationSection.
     *
     * @param path         path to retrieve
     * @param defaultValue default value to return
     * @return int representation of object, or the provided default value if it does not exist
     */
    public int getInteger(String path, int defaultValue);

    /**
     * Check to see if the path being requested is natively represented as a Integer.
     *
     * @param path path to check
     * @return true if the value is a native Integer
     */
    public boolean isInteger(String path);

    /**
     * Get a Double object from the ConfigurationSection.
     *
     * @param path path to retrieve
     * @return double representation of object, or null if it does not exist
     */
    public double getDouble(String path);

    /**
     * Get a Double object from the ConfigurationSection.
     *
     * @param path         path to retrieve
     * @param defaultValue default value to return
     * @return double representation of object, or the provided default value if it does not exist
     */
    public double getDouble(String path, double defaultValue);

    /**
     * Check to see if the path being requested is natively represented as a Double.
     *
     * @param path path to check
     * @return true if the value is a native Double
     */
    public boolean isDouble(String path);

    /**
     * Get a Long object from the ConfigurationSection.
     *
     * @param path path to retrieve
     * @return long representation of object, or null if it does not exist
     */
    public long getLong(String path);

    /**
     * Get a Long object from the ConfigurationSection.
     *
     * @param path         path to retrieve
     * @param defaultValue default value to return
     * @return long representation of object, or the provided default value if it does not exist
     */
    public long getLong(String path, long defaultValue);

    /**
     * Check to see if the path being requested is natively represented as a Long.
     *
     * @param path path to check
     * @return true if the value is a native Long
     */
    public boolean isLong(String path);

    /**
     * Get a Boolean object from the ConfigurationSection.
     *
     * @param path path to retrieve
     * @return boolean representation of object, or null if it does not exist
     */
    public boolean getBoolean(String path);

    /**
     * Get a Boolean object from the ConfigurationSection.
     *
     * @param path         path to retrieve
     * @param defaultValue default value to return
     * @return boolean representation of object, or the provided default value if it does not exist
     */
    public boolean getBoolean(String path, boolean defaultValue);

    /**
     * Check to see if the path being requested is natively represented as a Boolean.
     *
     * @param path path to check
     * @return true if the value is a native Boolean
     */
    public boolean isBoolean(String path);

    /**
     * Get a ConfigurationSection from this ConfigurationSection.
     *
     * @param path path to retrieve
     * @return ConfigurationSection, or null if not found
     */
    public ConfigurationSection getConfigurationSection(String path);

    /**
     * Create a new ConfigurationSection at the path provided.
     *
     * @param path path to create
     * @return ConfigurationSection created
     */
    public ConfigurationSection createConfigurationSection(String path);

    /**
     * Check to see if the path being requested is a ConfigurationSection.
     *
     * @param path path to check
     * @return true if the value is a ConfigurationSection
     */
    public boolean isConfigurationSection(String path);

    /**
     * Remove any path from a ConfigurationSection.
     *
     * @param path path to check
     */
    public void remove(String path);
}