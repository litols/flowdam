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
package uk.ac.lancs.stopcock.proxy;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import uk.ac.lancs.stopcock.netty.OpenFlowChannelInitializer;
import uk.ac.lancs.stopcock.openflow.Type;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
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

    /** Milliseconds before a Channel should be considered dead from lack of messages. */
    private long idleReadTimeout = 2500;
    /** Milliseconds before a Channel should send a ECHO request if its idle. */
    private long idleWriteTimeout = 500;

    /* Map to link channels to a proxied connection. */
    private Map<Channel, ProxiedConnection> proxiedConnections = new HashMap<>();

    /* List of all OpenFlow message types to log. */
    private List<Type> loggedTypes;

    /**
     * Create a new Proxy object which will automatically be capable of handling incoming connections.
     *
     * @param listenOn host/port to listen for connections on
     * @param connectTo host/port to connect out to
     * @param loggedTypes list of OpenFlow message types to log
     */
    public Proxy(InetSocketAddress listenOn, InetSocketAddress connectTo, List<Type> loggedTypes) {
        this.listenOn = listenOn;
        this.connectTo = connectTo;
        this.loggedTypes = loggedTypes;

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
        ProxiedConnection proxiedConnection = new ProxiedConnection(this, uniqueIDSource.incrementAndGet());
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

    /**
     * Get the number of milliseconds before a read timeout should be declared on a Netty channel. Exceeding this value
     * results in a channel being closed.
     *
     * @return number of milliseconds before a read timeout occurs
     */
    public long getIdleReadTimeout() {
        return idleReadTimeout;
    }

    /**
     * Get the number of milliseconds before a write timeout should be declared on a Netty channel. Exceeding this value
     * results in an ECHO request being sent.
     *
     * @return number of milliseconds before a write timeout occurs
     */
    public long getIdleWriteTimeout() {
        return idleWriteTimeout;
    }

    /**
     * Check to see if the proxy should log the message type provided.
     *
     * @param type message type to check if it's logged
     * @return true if it should be logged
     */
    public boolean isLogged(Type type) {
        return loggedTypes.contains(type);
    }
}
