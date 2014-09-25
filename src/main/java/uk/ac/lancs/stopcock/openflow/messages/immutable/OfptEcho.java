/*
 * Stopcock - Copyright © 2014 - Lancaster University
 *
 * All rights reserved, unauthorised distribution of source code, compiled
 * binary or usage is expressly prohibited.
 */
package uk.ac.lancs.stopcock.openflow.messages.immutable;

import uk.ac.lancs.stopcock.openflow.messages.OFPT;

/**
 * Abstract class for echo requests/replies, storing payload.
 */
public abstract class OFPTEcho extends OFPT {
    /* Payload of echo packet. */
    private byte[] payload;

    /**
     * Construct a new echo packet from the byte array from the OpenFlow packet.
     *
     * @param data non-head data
     */
    public OFPTEcho(byte[] data) {
        super(data);

        payload = data;
    }

    /**
     * Return the payload of the echo packet.
     *
     * @return byte array with data from echo packet
     */
    public byte[] getPayload() {
        return payload;
    }
}
