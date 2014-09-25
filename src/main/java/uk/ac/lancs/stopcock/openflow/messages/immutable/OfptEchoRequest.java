/*
 * Stopcock - Copyright © 2014 - Lancaster University
 *
 * All rights reserved, unauthorised distribution of source code, compiled
 * binary or usage is expressly prohibited.
 */
package uk.ac.lancs.stopcock.openflow.messages.immutable;

/**
 * Concrete class containing data for an echo request packet.
 */
public class OFPTEchoRequest extends OFPTEcho {
    /**
     * Construct a new echo request packet from the byte array from the OpenFlow packet.
     *
     * @param payload non-head data
     */
    public OFPTEchoRequest(byte[] payload) {
        super(payload);
    }
}
