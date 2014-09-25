/*
 * Stopcock - Copyright © 2014 - Lancaster University
 *
 * All rights reserved, unauthorised distribution of source code, compiled
 * binary or usage is expressly prohibited.
 */
package uk.ac.lancs.stopcock;

import uk.ac.lancs.stopcock.proxy.Proxy;

import java.net.InetSocketAddress;

/**
 * Main Stopcock entry point and management class.
 */
public class Stopcock {
    /**
     * Main entry point into Stopcock from the operating system.
     *
     * @param args arguments from operating system
     */
    public static void main(String[] args) {
        /* TODO: Configuration system for loading. */
        Proxy proxy = new Proxy(new InetSocketAddress(Integer.parseInt(args[0])), new InetSocketAddress(args[1], Integer.parseInt(args[2])));
    }
}
