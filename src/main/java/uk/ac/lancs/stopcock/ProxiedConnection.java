/*
 * Stopcock - Copyright © 2014 - Lancaster University
 *
 * All rights reserved, unauthorised distribution of source code, compiled
 * binary or usage is expressly prohibited.
 */
package uk.ac.lancs.stopcock;

import io.netty.channel.Channel;
import uk.ac.lancs.stopcock.openflow.messages.Container;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * ProxiedConnection encapsulates the relationship between two Netty channels, the switch incoming connection and
 * controller outgoing connection.
 */
public class ProxiedConnection {
    /** Netty channel used for the switch connection. */
    private Channel upstream;
    /** Netty channel used for the controller connection. */
    private Channel downstream;
    /** Flag to specify if the downstream connection has reached channelActive. */
    private boolean downstreamActive = false;
    /** Queue for outgoing packets to controller which could not yet be sent. */
    private Queue<Container> downstreamQueue = new ArrayDeque<>();

    /**
     * Register the upstream channel against this proxied connection.
     *
     * @param upstreamChannel upstream channel to register
     */
    public synchronized void registerUpstream(Channel upstreamChannel) {
        upstream = upstreamChannel;
    }

    /**
     * Register the downstream channel against this proxied connection.
     *
     * @param downstreamChannel downstream channel to register
     */
    public synchronized void registerDownstream(Channel downstreamChannel) {
        downstream = downstreamChannel;
    }

    /**
     * Mark the downstream channel as active, this should be called once channelActive has been called by Netty, it will
     * result in the release of any queued packets which have been buffered from the upstream.
     */
    public synchronized void activeDownstream() {
        downstreamActive = true;

        Container container;

        /* Purge any queued containers. */
        while ((container = downstreamQueue.poll()) != null) {
            downstream.writeAndFlush(container);
        }
    }

    /**
     * Unregister the upstream from this proxied connection. As OpenFlow has no ability to resolve state once a
     * connection has come or gone this must also close the downstream connection if there is one.
     */
    public synchronized void unregisterUpstream() {
        upstream = null;

        if (downstream != null) {
            downstream.close();
        }
    }

    /**
     * Unregister the downstream from this proxied connection. As OpenFlow has no ability to resolve state once a
     * connection has come or gone this must also close the upstream connection if there is one.
     */
    public synchronized void unregisterDownstream() {
        downstream = null;
        downstreamActive = false;

        if (upstream != null) {
            upstream.close();
        }
    }

    /**
     * Receive an OpenFlow Container (with header, data and possible message) from the upstream channel, it must be
     * relayed to the downstream channel (if open) alternatively it must be queued.
     *
     * @param upstreamContainer the received container
     */
    public synchronized void receiveUpstream(Container upstreamContainer) {
        if (downstream == null || !downstreamActive) {
            downstreamQueue.add(upstreamContainer);
        } else {
            downstream.writeAndFlush(upstreamContainer);
        }
    }

    /**
     * Receive an OpenFlow Container (with header, data and possible message) from the downstream channel, it must be
     * relayed to the upstream channel.
     *
     * @param downstreamContainer the received container
     */
    public synchronized void receiveDownstream(Container downstreamContainer) {
        upstream.writeAndFlush(downstreamContainer);
    }
}
