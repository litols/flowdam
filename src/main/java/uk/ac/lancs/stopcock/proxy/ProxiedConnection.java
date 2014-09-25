/*
 * Stopcock - Copyright © 2014 - Lancaster University
 *
 * All rights reserved, unauthorised distribution of source code, compiled
 * binary or usage is expressly prohibited.
 */
package uk.ac.lancs.stopcock.proxy;

import io.netty.channel.Channel;
import uk.ac.lancs.stopcock.openflow.Container;
import uk.ac.lancs.stopcock.openflow.Type;
import uk.ac.lancs.stopcock.openflow.messages.switchconfiguration.OFPTFeaturesReply;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * ProxiedConnection encapsulates the relationship between two Netty channels, the switch incoming connection and
 * controller outgoing connection.
 */
public class ProxiedConnection {
    /** Unique connection ID. */
    private int uniqueId;
    /** Datapath ID */
    private byte[] datapathId = new byte[8];
    /** Netty channel used for the switch connection. */
    private Channel upstream;
    /** Netty channel used for the controller connection. */
    private Channel downstream;
    /** Flag to specify if the downstream connection has reached channelActive. */
    private boolean downstreamActive = false;
    /** Queue for outgoing packets to controller which could not yet be sent. */
    private Queue<Container> downstreamQueue = new ArrayDeque<>();

    /**
     * Construct a new ProxiedConnection with a unique ID for reference and log tracking.
     *
     * @param uniqueId unique ID to identify this connection
     */
    public ProxiedConnection(int uniqueId) {
        this.uniqueId = uniqueId;
    }

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

        if (upstreamContainer.getMessageType() == Type.OFPT_FEATURES_REPLY) {
            OFPTFeaturesReply featuresReply = (OFPTFeaturesReply) upstreamContainer.getPacket();
            setDatapathId(featuresReply.getDatapathId());
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

    /**
     * Get the unique ID number which refers to this particular connection.
     *
     * @return unique ID
     */
    public int getUniqueId() {
        return uniqueId;
    }

    /**
     * Set the datapath ID handled by this proxied connection.
     *
     * @param datapathId the datapath ID being handled
     */
    public void setDatapathId(byte[] datapathId) {
        this.datapathId = datapathId;
    }

    /**
     * Return the datapath ID handed by this proxied connection.
     *
     * @return the datapath ID being handled, or a zeroed byte[8] if not learnt yet
     */
    public byte[] getDatapathId() {
        return datapathId;
    }
}
