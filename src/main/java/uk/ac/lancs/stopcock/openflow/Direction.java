/*
 * Stopcock - Copyright © 2014 - Lancaster University
 *
 * All rights reserved, unauthorised distribution of source code, compiled
 * binary or usage is expressly prohibited.
 */
package uk.ac.lancs.stopcock.openflow;

/**
 * Direction of different Message Types, using odd OpenFlow names.
 */
public enum Direction {
    /** Symmetric, can be sent either direction. */
    SYMMETRIC,
    /** Controller->Switch, sent from the controller to switch. */
    CONTROLLER_SWITCH,
    /** Asynchronous, sent from switch to controller. */
    ASYNCHRONOUS
}
