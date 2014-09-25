/*
 * Stopcock - Copyright © 2014 - Lancaster University
 *
 * All rights reserved, unauthorised distribution of source code, compiled
 * binary or usage is expressly prohibited.
 */
package uk.ac.lancs.stopcock.openflow.messages.switchconfiguration;

import uk.ac.lancs.stopcock.openflow.messages.OFPT;

import java.nio.ByteBuffer;

/**
 * Concrete class containing data for a features reply packet.
 */
public class OFPTFeaturesReply extends OFPT {
    /** Datapath ID from the switch. */
    private byte[] datapathId = new byte[8];
    /** Number of packet buffers available. */
    private int buffers;
    /** Number of tables supported by switch. */
    private byte tables;
    /** Auxiliary connection ID. */
    private byte auxiliaryId;
    /** Capabilities bitmap. */
    private int capabilities;

    /**
     * Construct a new features reply packet from the byte array from the OpenFlow packet.
     *
     * @param data non-head data
     */
    public OFPTFeaturesReply(byte[] data) {
        super(data);

        ByteBuffer byteBuffer = ByteBuffer.wrap(data);

        byteBuffer.get(datapathId);
        buffers = byteBuffer.getInt();
        tables = byteBuffer.get();
        auxiliaryId = byteBuffer.get();
        byteBuffer.getShort();
        capabilities = byteBuffer.getInt();
    }

    /**
     * Get the switches datapath ID.
     *
     * @return switches datapath ID
     */
    public byte[] getDatapathId() {
        return datapathId;
    }

    /**
     * Get the number of packet buffers which are available at once from the switch.
     *
     * @return number of packet buffers
     */
    public int getBuffers() {
        return buffers;
    }

    /**
     * Get the number of tables supported by the switch.
     *
     * @return number of supported tables
     */
    public byte getTables() {
        return tables;
    }

    /**
     * Get the auxiliary connection ID.
     *
     * @return auxiliary connection ID
     */
    public byte getAuxiliaryId() {
        return auxiliaryId;
    }

    /**
     * Get the capabilities bitmap.
     *
     * @return capabilities bitmap
     */
    public int getCapabilities() {
        return capabilities;
    }
}
