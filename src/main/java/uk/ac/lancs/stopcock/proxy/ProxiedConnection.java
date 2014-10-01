/*
 * Stopcock - Copyright © 2014 - Lancaster University
 *
 * All rights reserved, unauthorised distribution of source code, compiled
 * binary or usage is expressly prohibited.
 */
package uk.ac.lancs.stopcock.proxy;

import io.netty.channel.Channel;
import org.projectfloodlight.openflow.protocol.OFFeaturesReply;
import uk.ac.lancs.stopcock.openflow.Container;
import uk.ac.lancs.stopcock.openflow.Type;

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
    private byte[] datapathId;
    /** Datapath ID String Representation. */
    private String datapathIdString;
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
        setDatapathId(new byte[8]);
    }

    /**
     * Register the upstream channel against this proxied connection.
     *
     * @param upstreamChannel upstream channel to register
     */
    public synchronized void registerUpstream(Channel upstreamChannel) {
        upstream = upstreamChannel;
        log(" Incoming Upstream Switch Connected: " + upstream.remoteAddress());
    }

    /**
     * Register the downstream channel against this proxied connection.
     *
     * @param downstreamChannel downstream channel to register
     */
    public synchronized void registerDownstream(Channel downstreamChannel) {
        downstream = downstreamChannel;

        log(" Outgoing Upstream Switch Connecting");
    }

    /**
     * Mark the downstream channel as active, this should be called once channelActive has been called by Netty, it will
     * result in the release of any queued packets which have been buffered from the upstream.
     */
    public synchronized void activeDownstream() {
        downstreamActive = true;
        log(" Outgoing Downstream Controller Connected: " + downstream.remoteAddress());

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

        log(" Incoming Upstream Switch Disconnected");

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

        log(" Outgoing Downstream Controller Disconnected");

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

        /* Record the datapath ID if it passed through. */
        if (upstreamContainer.getMessageType() == Type.OFPT_FEATURES_REPLY) {
            OFFeaturesReply ofFeaturesReply = (OFFeaturesReply) upstreamContainer.getPacket();
            setDatapathId(ofFeaturesReply.getDatapathId().getBytes());
        }

        log(false, upstreamContainer);
    }

    /**
     * Receive an OpenFlow Container (with header, data and possible message) from the downstream channel, it must be
     * relayed to the upstream channel.
     *
     * @param downstreamContainer the received container
     */
    public synchronized void receiveDownstream(Container downstreamContainer) {
        upstream.writeAndFlush(downstreamContainer);
        log(true, downstreamContainer);
    }

    public void log(boolean fromController, Container container) {
        log("[" + (fromController ? "C->S" : "S->C") + "][" + container.getHeader().getTransactionId() + "][" + container.getMessageType() + "]" + container.getPacket().toString());
    }

    public void log(String log) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("[");
        stringBuilder.append(System.currentTimeMillis());
        stringBuilder.append("][");
        stringBuilder.append(uniqueId);
        stringBuilder.append("][");
        stringBuilder.append(datapathIdString);
        stringBuilder.append("]");
        stringBuilder.append(log);

        System.out.println(stringBuilder);
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

        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < 8; i++) {
            stringBuilder.append(String.format("%02x", datapathId[i]));
        }

        datapathIdString = stringBuilder.toString();
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
