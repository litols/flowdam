/*
 * Stopcock - Copyright © 2014 - Lancaster University
 *
 * All rights reserved, unauthorised distribution of source code, compiled
 * binary or usage is expressly prohibited.
 */
package uk.ac.lancs.stopcock;

import uk.ac.lancs.stopcock.configuration.ConfigurationSection;
import uk.ac.lancs.stopcock.configuration.YAMLConfigurationHandler;
import uk.ac.lancs.stopcock.openflow.Type;
import uk.ac.lancs.stopcock.proxy.Proxy;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main Stopcock entry point and management class.
 */
public class Stopcock {
    private static Map<String,Proxy> proxies = new HashMap<>();

    /**
     * Main entry point into Stopcock from the operating system.
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
            System.out.println("No proxies.");
            /* Fail - No Proxies. */
            return;
        }

        ConfigurationSection proxiesConfig = config.getConfigurationSection("proxies");

        for (String proxyName : proxiesConfig.getKeys(false)) {
            System.out.println("Reading " + proxyName);
            ConfigurationSection proxyConfig = proxiesConfig.getConfigurationSection(proxyName);

            if (!proxyConfig.isSet("localPort") || !proxyConfig.isSet("remotePort") || !proxyConfig.isSet("remoteAddress")) {
                System.out.println("Missing Critical " + proxyName);
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
            System.out.println("Started " + proxyName);
        }
    }
}
