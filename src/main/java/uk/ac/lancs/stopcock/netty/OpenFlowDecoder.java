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
import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFMessage;
import uk.ac.lancs.stopcock.openflow.Container;
import uk.ac.lancs.stopcock.openflow.Header;
import uk.ac.lancs.stopcock.openflow.Type;

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
        short typeId = byteBuf.readUnsignedByte();
        int length = byteBuf.readUnsignedShort();
        long transactionId = byteBuf.readUnsignedInt();

        /* Sanity check that we have enough data. */
        if (byteBuf.readableBytes() < (length - 8)) {
            throw new IllegalStateException();
        }

        /* Copy the data portion of the OpenFlow packet and store it. */
        byteBuf.resetReaderIndex();
        byte[] originalData = byteBuf.readBytes(length).array();

        /* Construct the OpenFlow header and container for both it and data. */
        Header header = new Header(version, typeId, length, transactionId);
        Type type = Type.getById(typeId);

        /* Get full openflow packet for processing with openflowj */
        byteBuf.resetReaderIndex();

        /* Call openflowj using our Netty 3.9.X -> Netty 4.0.0 proxy object. */
        OFMessage message = OFFactories.getGenericReader().readFrom(new NettyCompatibilityChannelBuffer(byteBuf));

        /* Container object for header, raw data and openflowj message. */
        Container container = new Container(header, originalData, type, message);

        /* Add to the Netty pipeline. */
        objects.add(container);
    }
}
