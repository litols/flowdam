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

import com.leafgraph.flowdam.openflow.Container;
import com.leafgraph.flowdam.proxy.ProxiedConnection;
import com.leafgraph.flowdam.proxy.Proxy;
import io.netty.channel.ChannelHandlerContext;
import org.projectfloodlight.openflow.protocol.OFMessage;

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
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);

        /* As the upstream Channel registered this new downstream in the proxy already, it must now be marked as
         * active to allow release of queued Containers. */
        ProxiedConnection proxiedConnection = proxy.getProxiedConnection(ctx.channel());
        proxiedConnection.activeDownstream();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Container container) throws Exception {
        ProxiedConnection proxiedConnection = proxy.getProxiedConnection(ctx.channel());

        // do anything to OpenFlow messages here!

        super.channelRead0(ctx, container);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);

        /* Unregister self in case of channel close. */
        proxy.unregisterDownstream(ctx.channel());
    }
}
