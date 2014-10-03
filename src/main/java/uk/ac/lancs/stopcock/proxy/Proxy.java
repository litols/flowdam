/*
 * Stopcock - Copyright © 2014 - Lancaster University
 *
 * All rights reserved, unauthorised distribution of source code, compiled
 * binary or usage is expressly prohibited.
 */
package uk.ac.lancs.stopcock.proxy;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import uk.ac.lancs.stopcock.netty.OpenFlowChannelInitializer;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A Proxy object is responsible for managing ProxiedConnections through it, it establishes the relationship between
 * an incoming connection to an outgoing connection.
 */
public class Proxy {
    /** Unique ID number for connection tracking. */
    private AtomicInteger uniqueIDSource = new AtomicInteger(0);
    /** Group for handling incoming connections (at the bind()/accept() level). */
    private EventLoopGroup bossGroup = new NioEventLoopGroup();
    /** Group for handling all connections after they have been accept()'d. */
    private EventLoopGroup workerGroup = new NioEventLoopGroup();

    /** Bootstrap for listening and accepting. */
    private ServerBootstrap serverBootstrap = new ServerBootstrap();
    /** Bootstrap outgoing connections to controllers. */
    private Bootstrap clientBootstrap = new Bootstrap();

    /** Host/port pair to listen for connections on. */
    private InetSocketAddress listenOn;
    /** Host/port pair for outgoing connections. */
    private InetSocketAddress connectTo;

    /* Map to link channels to a proxied connection. */
    private Map<Channel, ProxiedConnection> proxiedConnections = new HashMap<>();

    /**
     * Create a new Proxy object which will automatically be capable of handling incoming connections.
     *
     * @param listenOn host/port to listen for connections on
     * @param connectTo host/port to connect out to
     */
    public Proxy(InetSocketAddress listenOn, InetSocketAddress connectTo) {
        this.listenOn = listenOn;
        this.connectTo = connectTo;

        /* Set up Netty groups, channels and pipelines. */
        serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).childHandler(new OpenFlowChannelInitializer(this, false)).option(ChannelOption.TCP_NODELAY, true);
        clientBootstrap.group(workerGroup).channel(NioSocketChannel.class).handler(new OpenFlowChannelInitializer(this, true)).option(ChannelOption.TCP_NODELAY, true);

        /* Begin proxy. */
        serverBootstrap.bind(listenOn);
    }

    /**
     * Get an InetSocketAddress where to connect to.
     *
     * @return InetSocketAddress to connect onwards to
     */
    public InetSocketAddress getConnectTo() {
        return connectTo;
    }

    /**
     * Create a new Netty channel which is an outgoing connection to the onwards controller.
     *
     * @return Channel a initialised channel which needs to be connected.
     */
    public Channel onwardsChannel() {
        return clientBootstrap.bind(new InetSocketAddress(0)).channel();
    }

    /**
     * Register a new upstream connection (from switch), which will result in a new ProxiedConnection being created.
     *
     * @param newUpstream new upstream channel
     * @return ProxiedConnection representing this new Upstream connection
     */
    public synchronized ProxiedConnection registerUpstream(Channel newUpstream) {
        ProxiedConnection proxiedConnection = new ProxiedConnection(uniqueIDSource.incrementAndGet());
        proxiedConnections.put(newUpstream, proxiedConnection);

        proxiedConnection.registerUpstream(newUpstream);

        return proxiedConnection;
    }

    /**
     * Register a new downstream channel to the ProxiedConnection which is represented using the upstream channel.
     *
     * @param newDownstream new downstream channel
     * @param existingUpstream the upstream channel during which the new downstream channel was created
     * @return ProxiedConnection representing this new Downstream/existing Upstream connection
     */
    public synchronized ProxiedConnection registerDownstream(Channel newDownstream, Channel existingUpstream) {
        ProxiedConnection proxiedConnection = getProxiedConnection(existingUpstream);
        proxiedConnections.put(newDownstream, proxiedConnection);

        proxiedConnection.registerDownstream(newDownstream);

        return proxiedConnection;
    }

    /**
     * Unregister the upstream channel.
     *
     * @param channel upstream channel to unregister
     */
    public synchronized void unregisterUpstream(Channel channel) {
        ProxiedConnection proxiedConnection = proxiedConnections.remove(channel);

        if (proxiedConnection != null) {
            proxiedConnection.unregisterUpstream();
        }
    }

    /**
     * Unregister the downstream channel.
     *
     * @param channel downstream channel to unregister
     */
    public synchronized void unregisterDownstream(Channel channel) {
        ProxiedConnection proxiedConnection = proxiedConnections.remove(channel);

        if (proxiedConnection != null) {
            proxiedConnection.unregisterDownstream();
        }
    }

    /**
     * Fetch the ProxiedConnection identified by the Netty channel provided.
     *
     * @param channel channel to find ProxiedConnection based upon
     * @return ProxiedConnection identified by Channel, or null if not found
     */
    public synchronized ProxiedConnection getProxiedConnection(Channel channel) {
        return proxiedConnections.get(channel);
    }
}
