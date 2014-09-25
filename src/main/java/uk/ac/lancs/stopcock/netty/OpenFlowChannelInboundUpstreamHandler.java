package uk.ac.lancs.stopcock.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import uk.ac.lancs.stopcock.proxy.ProxiedConnection;
import uk.ac.lancs.stopcock.proxy.Proxy;
import uk.ac.lancs.stopcock.openflow.messages.Container;

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
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Container container) throws Exception {
        /* Send the Container via the proxy onwards. */
        ProxiedConnection proxiedConnection = proxy.getProxiedConnection(channelHandlerContext.channel());
        proxiedConnection.receiveUpstream(container);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);

        /* Register self in the proxy. */
        final Channel upstreamChannel = ctx.channel();
        proxy.registerUpstream(upstreamChannel);

        /* Ask the proxy to create the onwards Channel. */
        ChannelFuture future = proxy.connectOnwards();

        /* Register onwards channel against self in proxy. */
        final Channel downstreamChannel = future.channel();
        proxy.registerDownstream(downstreamChannel, upstreamChannel);

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
