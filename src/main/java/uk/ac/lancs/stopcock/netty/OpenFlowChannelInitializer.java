/*
 * Stopcock - Copyright © 2014 - Lancaster University
 *
 * All rights reserved, unauthorised distribution of source code, compiled
 * binary or usage is expressly prohibited.
 */
package uk.ac.lancs.stopcock.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import uk.ac.lancs.stopcock.proxy.Proxy;

import java.util.concurrent.TimeUnit;

/**
 * OpenFlowChannelInitializer provides the facility to setup up a Netty connection capable of processing OpenFlow
 * messages.
 */
public class OpenFlowChannelInitializer extends ChannelInitializer<SocketChannel> {
    /** Maximum possible length of a single OpenFlow message, as dictated by using uint16_t in the header. */
    public static int OPENFLOW_MAXIMUM_FRAME = (int) Math.pow(2, 16);

    /** Proxy to handle new connections initialized by this. */
    private Proxy proxy;
    /** If this Initializer is responsible for creating downstream channels. */
    private boolean downstream;

    /**
     * Constructs a new ChannelInitializer ready for initializing channels.
     *
     * @param proxy the proxy object that new channels belong to
     * @param downstream if channels created by this initializer are downsteam, if not then using a different last
     *                   entry in the pipeline
     */
    public OpenFlowChannelInitializer(Proxy proxy, boolean downstream) {
        this.proxy = proxy;
        this.downstream = downstream;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();

        /* Use Netty's prebuilt tools to handle frame separation on incoming data. */
        pipeline.addLast("lengthDecoder", new LengthFieldBasedFrameDecoder(OPENFLOW_MAXIMUM_FRAME, 2, 2, -4, 0));

        /* Process OpenFlow packets. */
        pipeline.addLast("openflowDecoder", new OpenFlowDecoder());
        pipeline.addLast("openflowEncoder", new OpenFlowEncoder());

        /* Idle Handler, prevent a hung switch or controller from disrupting traffic.  */
        pipeline.addLast("idleStateHandler", new IdleStateHandler(proxy.getIdleReadTimeout(), proxy.getIdleWriteTimeout(), 0, TimeUnit.MILLISECONDS));

        /* OpenFlow Processor. */
        if (downstream) {
            pipeline.addLast("messageHandler", new OpenFlowChannelInboundDownstreamHandler(proxy));
        } else {
            pipeline.addLast("messageHandler", new OpenFlowChannelInboundUpstreamHandler(proxy));
        }
    }
}
