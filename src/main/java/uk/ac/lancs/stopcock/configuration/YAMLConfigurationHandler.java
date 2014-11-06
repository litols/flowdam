/*
 * Copyright 2014 University of Lancaster
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.lancs.stopcock.configuration;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The {@code YAMLConfigurationHandler} acts as a gateway between files on disk and the ConfigurationSection structure,
 * it borrows heavily in ideas from Bukkit. It does copy some areas but not to the point it would count as a derivative
 * work.
 */
public class YAMLConfigurationHandler {
    /** Common YAML handler. */
    private Yaml yaml;

    /**
     * Construct a YAMLConfigurationHandler which can import/export ConfigurationSection's as YAML data.
     */
    public YAMLConfigurationHandler() {
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setIndent(2);
        dumperOptions.setAllowUnicode(true);
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        yaml = new Yaml(dumperOptions);
    }

    /**
     * Load YAML from a file into a ConfigurationSection.
     *
     * @param file file to load YAML from
     * @return a ConfigurationSection representing the YAML file
     * @throws YAMLException if the YAML file is damaged
     * @throws IOException if the file can not be read
     */
    public ConfigurationSection loadFromFile(File file) throws YAMLException, IOException {
        FileInputStream stream = new FileInputStream(file);
        InputStreamReader input = new InputStreamReader(stream, Charset.forName("UTF-8"));
        BufferedReader reader = new BufferedReader(input);

        StringBuilder builder = new StringBuilder();

        try {
            String line;

            while ((line = reader.readLine()) != null) {
                builder.append(line);
                builder.append('\n');
            }
        } finally {
            input.close();
        }

        return loadFromString(builder.toString());
    }

    /**
     * Save YAML representing a ConfigurationSection.
     *
     * @param file file to save YAML to
     * @param configurationSection configuration section to save
     * @throws IOException if the file can not be written
     */
    public void saveToFile(File file, ConfigurationSection configurationSection) throws IOException {
        Writer writer = new OutputStreamWriter(new FileOutputStream(file), Charset.forName("UTF-8"));

        try {
            writer.write(saveToString(configurationSection));
        } finally {
            writer.close();
        }
    }

    /**
     * Load YAML from a String int a ConfigurationSection.
     *
     * @param yamlString YAML string to load
     * @return a ConfigurationSection representing the YAML
     * @throws YAMLException if the YAML file is damaged
     */
    public ConfigurationSection loadFromString(String yamlString) throws YAMLException {
        Object object = yaml.load(yamlString);
        ConfigurationSection output = new MemorySection();

        if (object instanceof Map) {
            loadMap((Map) object, output);
        }

        return output;
    }

    /**
     * Export YAML representing a ConfigurationSection.
     *
     * @param configurationSection configuration section to save
     */
    public String saveToString(ConfigurationSection configurationSection) {
        return yaml.dump(saveMap(configurationSection));
    }

    /**
     * Export a ConfigurationSection to an abstract set of Maps and primitive values.
     *
     * @param output configuration section to output
     * @return abstract set of Maps and primitive values
     */
    private Map<?, ?> saveMap(ConfigurationSection output) {
        Map <String, Object> outputMap = new LinkedHashMap<>();

        for (String key : output.getKeys(false)) {
            Object obj = output.get(key);

            if (obj instanceof ConfigurationSection) {
                outputMap.put(key, saveMap((ConfigurationSection) obj));
            } else {
                outputMap.put(key, obj);
            }
        }

        return outputMap;
    }

    /**
     * Import a set of abstract Maps and primitive values into a ConfigurationSection
     *
     * @param map abstract set of Maps and primitive values to import
     * @param output configuration section from raw data
     */
    private void loadMap(Map<?, ?> map, ConfigurationSection output) {
        for (Map.Entry<?, ?> set : map.entrySet()) {
            String key = set.getKey().toString();
            Object value = set.getValue();

            if (value instanceof Map) {
                ConfigurationSection configurationSection = output.createConfigurationSection(key);
                loadMap((Map<?, ?>) value, configurationSection);
            } else {
                if (value instanceof Number || value instanceof String || value instanceof Boolean) {
                    output.set(key, value);
                }
            }
        }
    }
}