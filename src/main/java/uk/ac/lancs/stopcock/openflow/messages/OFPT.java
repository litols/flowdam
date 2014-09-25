/*
 * Stopcock - Copyright © 2014 - Lancaster University
 *
 * All rights reserved, unauthorised distribution of source code, compiled
 * binary or usage is expressly prohibited.
 */
package uk.ac.lancs.stopcock.openflow.messages;

/**
 * Abstract class for decoded OpenFlow packets.
 */
public abstract class OFPT {
    /**
     * Constructor which requires implementation to be compatible with automatic packet creation via the Type enum.
     *
     * @param data payload data to parse
     */
    public OFPT(byte[] data) {
    }
}
