/*
 * Stopcock - Copyright © 2014 - Lancaster University
 *
 * All rights reserved, unauthorised distribution of source code, compiled
 * binary or usage is expressly prohibited.
 */
package uk.ac.lancs.stopcock.openflow.messages.immutable;

/**
 * Concrete class containing data for an echo reply packet.
 */
public class OFPTEchoReply extends OFPTEcho {
    /**
     * Construct a new echo reply packet from the byte array from the OpenFlow packet.
     *
     * @param payload non-head data
     */
    public OFPTEchoReply(byte[] payload) {
        super(payload);
    }
}
