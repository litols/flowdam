/*
 * Copyright 2014 University of Lancaster
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.leafgraph.flowdam.openflow;

import org.projectfloodlight.openflow.protocol.OFMessage;

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
    /** OFMessage object representing the details of the packet. */
    private OFMessage packet;

    /**
     * Construct a new OpenFlow packet Container.
     *
     * @param header OpenFlow header
     * @param data raw data from packet, not including header bytes
     * @param packet an interpreted version of the data in a packet.
     */
    public Container(Header header, byte[] data, Type messageType, OFMessage packet) {
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
    public OFMessage getPacket() {
        return packet;
    }
}
