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
package uk.ac.lancs.stopcock.netty;

import io.netty.channel.ChannelHandlerContext;
import uk.ac.lancs.stopcock.proxy.ProxiedConnection;
import uk.ac.lancs.stopcock.proxy.Proxy;

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
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);

        /* Unregister self in case of channel close. */
        proxy.unregisterDownstream(ctx.channel());
    }
}
