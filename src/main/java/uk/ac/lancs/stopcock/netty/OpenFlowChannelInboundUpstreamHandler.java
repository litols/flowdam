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

        /* Register onwards channel against self in proxy, this needs to be done before the connect otherwise there
         * is a race condition. */
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
