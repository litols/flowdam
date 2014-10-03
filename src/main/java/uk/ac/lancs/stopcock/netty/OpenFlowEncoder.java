/*
 * Stopcock - Copyright © 2014 - Lancaster University
 *
 * All rights reserved, unauthorised distribution of source code, compiled
 * binary or usage is expressly prohibited.
 */
package uk.ac.lancs.stopcock.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import uk.ac.lancs.stopcock.openflow.Container;

import java.util.List;

/**
 * OpenFlowEncoder handles sending of Containers, which include a Header and raw data dump.
 */
class OpenFlowEncoder extends MessageToMessageEncoder<Container> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Container container, List<Object> objects) throws Exception {
        /* Construct a ByteBuf with the expected size of the output OpenFlow packet. */
        ByteBuf output = channelHandlerContext.alloc().buffer(container.getHeader().getLength());

        /* Write the binary data blob. */
        output.writeBytes(container.getData());

        /* Add back to the Netty pipeline. */
        objects.add(output);
    }
}
