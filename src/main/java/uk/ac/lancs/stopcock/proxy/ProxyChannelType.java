/*
 * Stopcock - Copyright © 2014 - Lancaster University
 *
 * All rights reserved, unauthorised distribution of source code, compiled
 * binary or usage is expressly prohibited.
 */
package uk.ac.lancs.stopcock.proxy;

/**
 * Describe the type of channel.
 */
public enum ProxyChannelType {
    /** Channel type is a switch. */
    SWITCH,
    /** Channel type is a controller. */
    CONTROLLER,
    /** Channel type doesn't really exist, sent to proxy. */
    PROXY
}
