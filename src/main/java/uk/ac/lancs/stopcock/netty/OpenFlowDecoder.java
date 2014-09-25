/*
 * Stopcock - Copyright © 2014 - Lancaster University
 *
 * All rights reserved, unauthorised distribution of source code, compiled
 * binary or usage is expressly prohibited.
 */
package uk.ac.lancs.stopcock.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import uk.ac.lancs.stopcock.openflow.messages.Container;
import uk.ac.lancs.stopcock.openflow.messages.Header;

import java.util.List;

/**
 * OpenFlowDecode is responsible for the first pass of decoding incoming OpenFlow packets and separating them into
 * the header and raw data. The pipeline will have already split up incoming data into individual packets.
 */
class OpenFlowDecoder extends MessageToMessageDecoder<ByteBuf> {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> objects) throws Exception {
        /* Sanity check. */
        if (byteBuf.readableBytes() < 8) {
            throw new IllegalStateException();
        }

        /* Read OpenFlow Header. */
        short version = byteBuf.readUnsignedByte();
        short type = byteBuf.readUnsignedByte();
        int length = byteBuf.readUnsignedShort();
        long transactionId = byteBuf.readUnsignedInt();

        /* Sanity check that we have enough data. */
        if (byteBuf.readableBytes() < (length - 8)) {
            throw new IllegalStateException();
        }

        byte[] data;

        /* Copy the data portion of the OpenFlow packet and store it. */
        if (length > 8) {
            data = byteBuf.readBytes(length - 8).array();
        } else {
            data = null;
        }

        /* Construct the OpenFlow header and container for both it and data. */
        Header header = new Header(version, type, length, transactionId);
        Container container = new Container(header, data);

        /* Add to the Netty pipeline. */
        objects.add(container);
    }
}
