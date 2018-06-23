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
package com.leafgraph.flowdam.netty;

import com.leafgraph.flowdam.Flowdam;
import com.leafgraph.flowdam.openflow.Container;
import com.leafgraph.flowdam.proxy.ProxiedConnection;
import com.leafgraph.flowdam.proxy.Proxy;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;

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

        /* Attempt connect. */
        ChannelFuture future = proxy.getClientBootstrap().connect(proxy.getConnectTo());

        final Channel downstreamChannel = future.channel();
        proxy.registerDownstream(downstreamChannel, upstreamChannel);

        future.awaitUninterruptibly();
        /* Add callback to handle connection failure, closing the upstream channel should the downstream fail. */
        future.addListener(channelFuture -> {
            if (!channelFuture.isSuccess()) {
                Flowdam.logger.info("downstream create failed.");
                channelFuture.cause().printStackTrace();
                upstreamChannel.close();
            }
        });
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Container container) throws Exception {
        ProxiedConnection proxiedConnection = proxy.getProxiedConnection(channelHandlerContext.channel());

        // do anything to OpenFlow messages here!

        super.channelRead0(channelHandlerContext, container);
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);

        /* Unregister self in case of channel close. */
        proxy.unregisterUpstream(ctx.channel());
    }
}
