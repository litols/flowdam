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
package com.leafgraph.flowdam;

import com.leafgraph.flowdam.proxy.Proxy;
import com.leafgraph.flowdam.configuration.ConfigurationSection;
import com.leafgraph.flowdam.configuration.YAMLConfigurationHandler;
import com.leafgraph.flowdam.openflow.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main Flowdam entry point and management class.
 */
public class Flowdam {
    private static Map<String,Proxy> proxies = new HashMap<>();

    public static Logger logger = LoggerFactory.getLogger(Flowdam.class);

    /**
     * Main entry point into Flowdam from the operating system.
     *
     * @param args arguments from operating system
     */
    public static void main(String[] args) {
        File configFile = new File("config.yml");

        if (args.length > 0) {
            configFile = new File(args[0]);
        }

        ConfigurationSection config;

        try {
            config = new YAMLConfigurationHandler().loadFromFile(configFile);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        if (!config.isConfigurationSection("proxies")) {
            logger.warn("No proxies.");
            /* Fail - No Proxies. */
            return;
        }

        ConfigurationSection proxiesConfig = config.getConfigurationSection("proxies");

        for (String proxyName : proxiesConfig.getKeys(false)) {
            logger.info("Reading " + proxyName);
            ConfigurationSection proxyConfig = proxiesConfig.getConfigurationSection(proxyName);

            if (!proxyConfig.isSet("localPort") || !proxyConfig.isSet("remotePort") || !proxyConfig.isSet("remoteAddress")) {
                logger.warn("Missing Critical " + proxyName);
                /* Fail this proxy . */
                continue;
            }

            InetSocketAddress localAddress;

            if (proxyConfig.isSet("localAddress")) {
                localAddress = new InetSocketAddress(proxyConfig.getString("localAddress"), proxyConfig.getInteger("localPort"));
            } else {
                localAddress = new InetSocketAddress(proxyConfig.getInteger("localPort"));
            }

            InetSocketAddress remoteAddress = new InetSocketAddress(proxyConfig.getString("remoteAddress"), proxyConfig.getInteger("remotePort"));

            List<Type> loggedTypes = new ArrayList<>();

            if (proxyConfig.isConfigurationSection("loggedTypes")) {
                ConfigurationSection loggedTypesConfig = proxyConfig.getConfigurationSection("loggedTypes");

                for (String typeName : loggedTypesConfig.getKeys(false)) {
                    Type type = Type.valueOf(typeName);

                    if (type == null) {
                        /* Fail */
                    } else {
                        loggedTypes.add(type);
                    }
                }
            }

            Proxy proxy = new Proxy(localAddress, remoteAddress, loggedTypes);
            proxies.put(proxyName, proxy);
            logger.info("Started " + proxyName);
        }
    }
}
