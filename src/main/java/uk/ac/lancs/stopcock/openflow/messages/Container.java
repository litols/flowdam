/*
 * Stopcock - Copyright © 2014 - Lancaster University
 *
 * All rights reserved, unauthorised distribution of source code, compiled
 * binary or usage is expressly prohibited.
 */
package uk.ac.lancs.stopcock.openflow.messages;

/**
 * Container object encapsulates an OpenFlow header, the raw data and any objects created from the raw data.
 */
public class Container {
    /** The standard 8 byte OpenFlow header. */
    private Header header;
    /** Raw data after the header, when relaying this is what should be sent */
    private byte[] data;

    /**
     * Construct a new OpenFlow packet Container.
     *
     * @param header OpenFlow header
     * @param data raw data from packet, not including header bytes
     */
    public Container(Header header, byte[] data) {
        this.header = header;
        this.data = data;
    }

    /**
     * Get OpenFlow Header.
     *
     * @return the OpenFlow Header as an object
     */
    public Header getHeader() {
        return header;
    }

    /**
     * Get the raw data from the packet, this does not include the OpenFlow header but is what should be sent onwards
     * when being relayed.
     *
     * @return byte array of the OpenFlow packet (not including header)
     */
    public byte[] getData() {
        return data;
    }
}
