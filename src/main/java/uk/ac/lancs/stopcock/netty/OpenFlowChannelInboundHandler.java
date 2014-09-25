/*
 * Stopcock - Copyright © 2014 - Lancaster University
 *
 * All rights reserved, unauthorised distribution of source code, compiled
 * binary or usage is expressly prohibited.
 */
package uk.ac.lancs.stopcock.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import uk.ac.lancs.stopcock.Proxy;
import uk.ac.lancs.stopcock.openflow.messages.Container;

/**
 * OpenFlowChannelInboundHandler looks after idle timeouts and other common functionality between an upstream and
 * downstream connection.
 */
public abstract class OpenFlowChannelInboundHandler extends SimpleChannelInboundHandler<Container> {
    /** Proxy this Handler uses for routing packets. */
    Proxy proxy;

    /**
     * Base constructor for an Inbound handler.
     *
     * @param proxy the proxy which is responsible for this channel
     */
    public OpenFlowChannelInboundHandler(Proxy proxy) {
        this.proxy = proxy;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);

        /* In case the connection becomes idle we must attempt to verify it is still alive. */
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            if (e.state() == IdleState.READER_IDLE) {
                /* No packets have been received in a reasonable time period and as such should now be closed. */
                ctx.close();
            } else if (e.state() == IdleState.WRITER_IDLE) {
                // TODO - Send echo request!
            }
        }
    }
}
