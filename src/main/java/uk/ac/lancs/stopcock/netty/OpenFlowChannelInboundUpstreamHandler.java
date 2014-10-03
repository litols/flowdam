/*
 * Stopcock - Copyright © 2014 - Lancaster University
 *
 * All rights reserved, unauthorised distribution of source code, compiled
 * binary or usage is expressly prohibited.
 */
package uk.ac.lancs.stopcock.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import uk.ac.lancs.stopcock.proxy.Proxy;

/**
 * OpenFlowChannelInboundUpstreamHandler is the end of the Netty pipeline for incoming connections from switches
 * wishing to participate in OpenFlow. Once the channel is active it attempts to create the onwards channel to the
 * controller.
 */
class OpenFlowChannelInboundUpstreamHandler extends OpenFlowChannelInboundHandler {
    /**
     * Constructs a new Handler ready to serve the Channel.
     *
     * @param proxy the proxy which is responsible for this channel
     */
    public OpenFlowChannelInboundUpstreamHandler(Proxy proxy) {
        super(proxy);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);

        /* Register self in the proxy. */
        final Channel upstreamChannel = ctx.channel();
        proxy.registerUpstream(upstreamChannel);

        /* Register onwards channel against self in proxy. */
        final Channel downstreamChannel = proxy.onwardsChannel();
        proxy.registerDownstream(downstreamChannel, upstreamChannel);

        /* Attempt connect. */
        ChannelFuture future = downstreamChannel.connect(proxy.getConnectTo());

        /* Add callback to handle connection failure, closing the upstream channel should the downstream fail. */
        future.addListener(channelFuture -> {
            if (!channelFuture.isSuccess()) {
                upstreamChannel.close();
            }
        });
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);

        /* Unregister self in case of channel close. */
        proxy.unregisterUpstream(ctx.channel());
    }
}
