/*
 * Stopcock - Copyright © 2014 - Lancaster University
 *
 * All rights reserved, unauthorised distribution of source code, compiled
 * binary or usage is expressly prohibited.
 */
package uk.ac.lancs.stopcock.openflow;

import uk.ac.lancs.stopcock.openflow.messages.OFPT;

/**
 * Container object encapsulates an OpenFlow header, the raw data and any objects created from the raw data.
 */
public class Container {
    /** The standard 8 byte OpenFlow header. */
    private Header header;
    /** Raw data after the header, when relaying this is what should be sent */
    private byte[] data;
    /** The message type. */
    private Type messageType;
    /** OFPT object representing the details of the packet. */
    private OFPT packet;

    /**
     * Construct a new OpenFlow packet Container.
     *
     * @param header OpenFlow header
     * @param data raw data from packet, not including header bytes
     * @param packet an interpreted version of the data in a packet.
     */
    public Container(Header header, byte[] data, Type messageType, OFPT packet) {
        this.header = header;
        this.data = data;
        this.messageType = messageType;
        this.packet = packet;
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

    /**
     * Get the messages Type.
     *
     * @return messages type
     */
    public Type getMessageType() {
        return messageType;
    }

    /**
     * Get the object representation of the packet.
     *
     * @return object representation of the packet
     */
    public OFPT getPacket() {
        return packet;
    }
}
