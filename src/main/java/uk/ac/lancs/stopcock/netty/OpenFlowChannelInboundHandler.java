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
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import uk.ac.lancs.stopcock.proxy.ProxiedConnection;
import uk.ac.lancs.stopcock.proxy.Proxy;
import uk.ac.lancs.stopcock.openflow.Container;
import uk.ac.lancs.stopcock.proxy.ProxyChannelType;

/**
 * OpenFlowChannelInboundHandler looks after idle timeouts and other common functionality between an upstream and
 * downstream connection.
 */
abstract class OpenFlowChannelInboundHandler extends SimpleChannelInboundHandler<Container> {
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
            ProxiedConnection proxiedConnection = proxy.getProxiedConnection(ctx.channel());

            /* Skip echos unless we're fully connected. */
            if (proxiedConnection.getDownstreamVersion() != null && proxiedConnection.getUpstreamVersion() != null) {
                IdleStateEvent e = (IdleStateEvent) evt;
                if (e.state() == IdleState.READER_IDLE) {
                /* No packets have been received in a reasonable time period and as such should now be closed. */
                    proxiedConnection.log(" Read timeout, " + proxiedConnection.getProxyChannelType(ctx.channel()) + ".");
                    ctx.close();
                } else if (e.state() == IdleState.WRITER_IDLE) {
                /* Construct an appropriate ping packet for this connections version and send it via proxy. */
                    proxiedConnection.send(ProxyChannelType.PROXY, ctx.channel(), proxiedConnection.createPing());
                }
            }
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Container container) throws Exception {
        /* Send the Container via the proxy onwards. */
        ProxiedConnection proxiedConnection = proxy.getProxiedConnection(channelHandlerContext.channel());
        proxiedConnection.receive(channelHandlerContext.channel(), container);
    }
}
