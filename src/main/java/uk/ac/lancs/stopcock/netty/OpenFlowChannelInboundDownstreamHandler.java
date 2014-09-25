package uk.ac.lancs.stopcock.netty;

import io.netty.channel.ChannelHandlerContext;
import uk.ac.lancs.stopcock.proxy.ProxiedConnection;
import uk.ac.lancs.stopcock.proxy.Proxy;
import uk.ac.lancs.stopcock.openflow.messages.Container;

/**
 * OpenFlowChannelInboundDownstreamHandler is the end of the Netty pipeline for outgoing connections to controllers
 * wishing to control via OpenFlow.
 */
class OpenFlowChannelInboundDownstreamHandler extends OpenFlowChannelInboundHandler {
    /**
     * Constructs a new Handler ready to serve the Channel.
     *
     * @param proxy the proxy which is responsible for this channel
     */
    public OpenFlowChannelInboundDownstreamHandler(Proxy proxy) {
        super(proxy);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Container container) throws Exception {
        /* Send the Container via the proxy onwards. */
        ProxiedConnection proxiedConnection = proxy.getProxiedConnection(channelHandlerContext.channel());
        proxiedConnection.receiveDownstream(container);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);

        /* As the upstream Channel registered this new downstream in the proxy already, it must now be marked as
         * active to allow release of queued Containers. */
        ProxiedConnection proxiedConnection = proxy.getProxiedConnection(ctx.channel());
        proxiedConnection.activeDownstream();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);

        /* Unregister self in case of channel close. */
        proxy.unregisterDownstream(ctx.channel());
    }
}
