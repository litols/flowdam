/*
 * YAML/Memory Configuration Handling - Copyright © 2014 - Peter Wood <peter@alastria.net>
 *
 * Under license to Lancaster University for academic and non commercial use in the Stopcock project from
 * Peter Wood <peter@alastria.net>, any distribution in source or binary form outside Lancaster University
 * is expressly prohibited. Configuration system was written solely outside Lancaster University, all rights
 * are reserved.
 */
package uk.ac.lancs.stopcock.configuration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MemorySection implements ConfigurationSection {
    /** Direct parent ConfigurationSection. */
    private ConfigurationSection parent = null;
    /** Root ConfigurationSection. */
    private ConfigurationSection root = null;
    /** Name of ConfigurationSection. */
    private String name = null;
    /** Internal data store. */
    private Map<String, Object> dataStore = new LinkedHashMap<>();

    /**
     * Construct a new root MemorySection.
     */
    public MemorySection() {
    }

    /**
     * Construct a new ConfigurationSection which belongs to the provided parent ConfigurationSection with the name
     * provided (which makes up part of its path).
     *
     * @param parent the parent configuration section
     * @param name the name of this configuration section
     */
    public MemorySection(ConfigurationSection parent, String name) {
        this.parent = parent;

        root = parent.getRoot();

        // As parent has a root of null, if we're a direct root descendant then we need to set our parent status.
        if (root == null) {
            root = parent;
        }

        this.name = name;
    }

    @Override
    public String getPath() {
        if (parent != null && parent != root) {
            return parent.getPath() + PATH_SEPARATOR + name;
        } else {
            return name;
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ConfigurationSection getRoot() {
        return root;
    }

    @Override
    public ConfigurationSection getParent() {
        return parent;
    }

    @Override
    public List<String> getKeys(boolean deep) {
        List<String> keys = new ArrayList<>();

        if (deep) {
            for(Map.Entry<String, Object> entry : dataStore.entrySet()) {
                if (entry.getValue() instanceof ConfigurationSection) {
                    List<String> subKeys = ((ConfigurationSection) entry.getValue()).getKeys(true);

                    for (String subKey : subKeys) {
                        keys.add(entry.getKey() + PATH_SEPARATOR + subKey);
                    }
                } else {
                    keys.add(entry.getKey());
                }
            }

        } else {
            keys.addAll(dataStore.keySet());
        }

        return keys;
    }

    @Override
    public void set(String path, Object value) {
        String key = getKey(path);

        ConfigurationSection configurationSection = seekPathContaining(path, true);

        if (this == configurationSection) {
            if (value instanceof Number || value instanceof Boolean || value instanceof String || value instanceof ConfigurationSection) {
                dataStore.put(key, value);
            } else {
                throw new IllegalArgumentException("'" + value.getClass() + "' can not be stored in a ConfigurationSection.");
            }
        } else {
            configurationSection.set(key, value);
        }
    }

    @Override
    public Object get(String path) {
        String key = getKey(path);

        ConfigurationSection configurationSection = seekPathContaining(path, true);

        if (this == configurationSection) {
            return dataStore.get(key);
        } else {
            return configurationSection.get(key);
        }
    }

    /**
     * Get the ConfigurationSection represented by the key before the last one, optionally allowing the path to be
     * created as it goes.
     *
     * @param path path to get
     * @param create set to true to create missing ConfigurationSections or replace none ConfigurationSection values
     * @return the ConfigurationSection found by the path, or null if it wasn't found/created
     */
    private ConfigurationSection seekPathContaining(String path, boolean create) {
        // Separate paths
        String[] pathParts = path.split(PATH_SEPARATOR_REGEX);

        if (pathParts.length <= 1) {
            return this;
        } else {
            // Find last part of path which is the actual variable name to set.
            int lastPart = pathParts.length - 1;

            // Go through the parts finding each configuration section.
            ConfigurationSection checking = this;

            for (int i = 0; i < lastPart; i++) {
                // Fetch a configuration section with the path part.
                ConfigurationSection find = checking.getConfigurationSection(pathParts[i]);

                // If it's null because not there OR not a ConfigurationSection create one.
                if (find == null) {
                    if (create) {
                        find = checking.createConfigurationSection(pathParts[i]);
                    } else {
                        return null;
                    }
                }

                checking = find;
            }

            return checking;
        }
    }

    /**
     * Get the final key name form the path provided.
     *
     * @param path path to parse
     * @return final key name
     */
    private String getKey(String path) {
        // Separate paths
        String[] pathParts = path.split(PATH_SEPARATOR_REGEX);

        if (pathParts.length <= 1) {
            return path;
        } else {
            return pathParts[pathParts.length - 1];
        }
    }

    @Override
    public boolean isSet(String path) {
        ConfigurationSection configurationSection = seekPathContaining(path, false);
        String key = getKey(path);

        if (this == configurationSection) {
            return dataStore.containsKey(key);
        } else {
            return configurationSection.isSet(key);
        }
    }

    @Override
    public String getString(String path) {
        return getString(path, null);
    }

    @Override
    public String getString(String path, String defaultValue) {
        Object object = get(path);

        if (object == null) {
            return defaultValue;
        }

        if (object instanceof String) {
            return (String) object;
        }

        return object.toString();
    }

    @Override
    public boolean isString(String path) {
        return (get(path) instanceof String);
    }

    @Override
    public int getInteger(String path) {
        return getInteger(path, 0);
    }

    @Override
    public int getInteger(String path, int defaultValue) {
        Object object = get(path);

        if (object == null) {
            return defaultValue;
        }

        if (object instanceof Integer) {
            return (Integer) object;
        }

        try {
            return Integer.valueOf(object.toString());
        } catch (NumberFormatException | NullPointerException e) {
            return defaultValue;
        }
    }

    @Override
    public boolean isInteger(String path) {
        return (get(path) instanceof Integer);
    }

    @Override
    public double getDouble(String path) {
        return getDouble(path, 0);
    }

    @Override
    public double getDouble(String path, double defaultValue) {
        Object object = get(path);

        if (object == null) {
            return defaultValue;
        }

        if (object instanceof Double) {
            return (Double) object;
        }

        try {
            return Double.valueOf(object.toString());
        } catch (NumberFormatException | NullPointerException e) {
            return defaultValue;
        }
    }

    @Override
    public boolean isDouble(String path) {
        return (get(path) instanceof Double);
    }

    @Override
    public long getLong(String path) {
        return getLong(path, 0);
    }

    @Override
    public long getLong(String path, long defaultValue) {
        Object object = get(path);

        if (object == null) {
            return defaultValue;
        }

        if (object instanceof Long) {
            return (Long) object;
        }

        try {
            return Long.valueOf(object.toString());
        } catch (NumberFormatException | NullPointerException e) {
            return defaultValue;
        }
    }

    @Override
    public boolean isLong(String path) {
        return (get(path) instanceof Long);
    }

    @Override
    public boolean getBoolean(String path) {
        return getBoolean(path, false);
    }

    @Override
    public boolean getBoolean(String path, boolean defaultValue) {
        Object object = get(path);

        if (object == null) {
            return defaultValue;
        }

        if (object instanceof Boolean) {
            return (Boolean) object;
        }

        return defaultValue;
    }

    @Override
    public boolean isBoolean(String path) {
        return (get(path) instanceof Boolean);
    }

    @Override
    public ConfigurationSection getConfigurationSection(String path) {
        Object object = get(path);

        if (object instanceof ConfigurationSection) {
            return (ConfigurationSection) object;
        }

        return null;
    }

    @Override
    public ConfigurationSection createConfigurationSection(String path) {
        String key = getKey(path);
        ConfigurationSection containing = seekPathContaining(path, true);

        ConfigurationSection output = new MemorySection(containing, key);
        containing.set(key, output);

        return output;
    }

    @Override
    public boolean isConfigurationSection(String path) {
        return (get(path) instanceof ConfigurationSection);
    }

    @Override
    public void remove(String path) {
        ConfigurationSection configurationSection = seekPathContaining(path, false);
        String key = getKey(path);

        if (this == configurationSection) {
            dataStore.remove(key);
        } else {
            configurationSection.remove(key);
        }
    }
}