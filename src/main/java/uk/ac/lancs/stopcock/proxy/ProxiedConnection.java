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

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.util.ReferenceCountUtil;
import org.projectfloodlight.openflow.protocol.OFEchoReply;
import org.projectfloodlight.openflow.protocol.OFEchoRequest;
import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFFeaturesReply;
import org.projectfloodlight.openflow.protocol.OFHello;
import org.projectfloodlight.openflow.protocol.OFVersion;
import uk.ac.lancs.stopcock.netty.NettyCompatibilityChannelBuffer;
import uk.ac.lancs.stopcock.openflow.Container;
import uk.ac.lancs.stopcock.openflow.Header;
import uk.ac.lancs.stopcock.openflow.Type;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ProxiedConnection encapsulates the relationship between two Netty channels, the switch incoming connection and
 * controller outgoing connection.
 */
public class ProxiedConnection {
    /** Echo data for our own echo requests/replies. */
    private static final byte[] ECHO_DATA = new byte[] { 0x53, 0x74, 0x6f, 0x70, 0x63, 0x6f, 0x63, 0x6b };

    /** Owning Proxy. */
    private Proxy owningProxy;
    /** Unique connection ID. */
    private int uniqueId;
    /** Datapath ID */
    private byte[] datapathId;
    /** Datapath ID String Representation. */
    private String datapathIdString;
    /** Netty channel used for the switch connection. */
    private Channel upstream;
    /** Upstream version. */
    private OFVersion upstreamVersion;
    /** Netty channel used for the controller connection. */
    private Channel downstream;
    /** Downstream version. */
    private OFVersion downstreamVersion;
    /** Flag to specify if the downstream connection has reached channelActive. */
    private boolean downstreamActive = false;
    /** Queue for outgoing packets to controller which could not yet be sent. */
    private Queue<Container> downstreamQueue = new ArrayDeque<>();

    /** Statistics on number of messages types received from upstream. */
    private Map<Type, AtomicInteger> upstreamReceived = new HashMap<>();
    /** Statistics on number of messages types received from downstream. */
    private Map<Type, AtomicInteger> downstreamReceived = new HashMap<>();

    /**
     * Construct a new ProxiedConnection with a unique ID for reference and log tracking.
     *
     * @param proxy which proxy object owns this connection
     * @param uniqueId unique ID to identify this connection
     */
    public ProxiedConnection(Proxy proxy, int uniqueId) {
        owningProxy = proxy;
        this.uniqueId = uniqueId;
        setDatapathId(new byte[8]);

        /* Add zero'd statistics. */
        for (Type type : Type.values()) {
            upstreamReceived.put(type, new AtomicInteger(0));
            downstreamReceived.put(type, new AtomicInteger(0));
        }
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
     * Construct a suitable OpenFlow echo request based upon the OpenFlow version the upstream channel advertised
     * version.
     *
     * @return Container with a suitable echo request
     */
    public Container createPing() {
        ByteBuf byteBuf = upstream.alloc().buffer(8);
        NettyCompatibilityChannelBuffer compatBuffer = new NettyCompatibilityChannelBuffer(byteBuf);

        OFEchoRequest request = OFFactories.getFactory(upstreamVersion).echoRequest(ECHO_DATA);
        request.writeTo(compatBuffer);

        byte[] rawData = new byte[compatBuffer.readableBytes()];

        compatBuffer.resetReaderIndex();
        compatBuffer.readBytes(rawData);

        ReferenceCountUtil.release(byteBuf);

        Header header = new Header((short) upstreamVersion.getWireVersion(), (short) Type.OFPT_ECHO_REQUEST.getId(), 8, request.getXid());
        return new Container(header, rawData, Type.OFPT_ECHO_REQUEST, request);
    }

    /**
     * Receive a container and process it, forwarding it onwards if required.
     *
     * @param incoming the channel the container was received upon
     * @param container the container being received
     */
    public synchronized void receive(Channel incoming, Container container) {
        ProxyChannelType channelSource = (incoming == upstream ? ProxyChannelType.SWITCH : ProxyChannelType.CONTROLLER);
        ProxyChannelType channelDestination = (incoming != upstream ? ProxyChannelType.SWITCH : ProxyChannelType.CONTROLLER);

        /* Intercept echo replies which are destined for the proxy, and as such shouldn't be forwarded. */
        if (container.getMessageType() == Type.OFPT_ECHO_REPLY) {
            OFEchoReply ofEchoReply = (OFEchoReply) container.getPacket();

            if (Arrays.equals(ECHO_DATA, ofEchoReply.getData())) {
                channelDestination = ProxyChannelType.PROXY;
            }
        }

        /* Intercept the HELLO and record the OpenFlow version. */
        if (container.getMessageType() == Type.OFPT_HELLO) {
            OFHello ofHello = (OFHello) container.getPacket();

            if (channelSource == ProxyChannelType.SWITCH) {
                upstreamVersion = ofHello.getVersion();
            } else {
                downstreamVersion = ofHello.getVersion();
            }
        }

        /* Record the datapath ID if it passed through. */
        if (container.getMessageType() == Type.OFPT_FEATURES_REPLY) {
            OFFeaturesReply ofFeaturesReply = (OFFeaturesReply) container.getPacket();
            setDatapathId(ofFeaturesReply.getDatapathId().getBytes());
        }

        log(channelSource, channelDestination, container);

        if (channelDestination != ProxyChannelType.PROXY) {
            send(channelSource, channelDestination, container);
        }
    }

    /**
     * Send a container out specifying the destination by a Channel, used for sending ECHO requests from the Netty
     * IdleStateHandler.
     *
     * @param channelSource channel type which is sending
     * @param destination the destination Netty channel to send the container to
     * @param container the container to send
     */
    public synchronized void send(ProxyChannelType channelSource, Channel destination, Container container) {
        ProxyChannelType channelDestination = (destination == upstream ? ProxyChannelType.SWITCH : ProxyChannelType.CONTROLLER);
        send(channelSource, channelDestination, container);
    }

    /**
     * Send a container out specifying the destination by a ChannelType.
     *
     * @param channelSource channel type which is sending
     * @param channelDestination channel type to send container to
     * @param container the container to send
     */
    public synchronized void send(ProxyChannelType channelSource, ProxyChannelType channelDestination, Container container) {
        Channel outputChannel = channelDestination == ProxyChannelType.SWITCH ? upstream : downstream;

        if (channelSource == ProxyChannelType.PROXY) {
            log(channelSource, channelDestination, container);
        }

        if ((outputChannel != downstream) || downstreamActive) {
            outputChannel.writeAndFlush(container);
        } else {
            downstreamQueue.add(container);
        }
    }

    /**
     * Log the contents of a container.
     *
     * @param channelSource where the source of this container was
     * @param channelDestination where the destination of this container is
     * @param container the container to be logged
     */
    public void log(ProxyChannelType channelSource, ProxyChannelType channelDestination, Container container) {
        /* Account messages received. */
        if (channelSource == ProxyChannelType.SWITCH) {
            upstreamReceived.get(container.getMessageType()).incrementAndGet();
        } else {
            downstreamReceived.get(container.getMessageType()).incrementAndGet();
        }

        if (owningProxy.isLogged(container.getMessageType())) {
            log("[" + channelSource + "->" + channelDestination + "][" + container.getHeader().getTransactionId() + "][" + container.getMessageType() + "]" + container.getPacket().toString());
        }
    }

    /**
     * Log generic text about this ProxiedConnection.
     *
     * @param log text to be logged
     */
    public void log(String log) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("[");
        stringBuilder.append(System.currentTimeMillis());
        stringBuilder.append("][");
        stringBuilder.append(uniqueId);
        stringBuilder.append("][");
        stringBuilder.append(datapathIdString);
        stringBuilder.append("][");

        if (upstream != null && upstream.remoteAddress() != null) {
            stringBuilder.append(upstream.remoteAddress());
        } else {
            stringBuilder.append("/0.0.0.0:0");
        }
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

    /**
     * Look up a ChannelType by providing the Channel.
     *
     * @param channel channel to be looked up to find the ChannelType
     * @return ChannelType identified by the channel
     */
    public ProxyChannelType getProxyChannelType(Channel channel) {
        return (channel == downstream ? ProxyChannelType.CONTROLLER : ProxyChannelType.SWITCH);
    }

    /**
     * Get the Upstream (Switch) OpenFlow version as dictated by the initial HELLO.
     *
     * @return OpenFlow version as dictated by the initial HELLO
     */
    public OFVersion getUpstreamVersion() {
        return upstreamVersion;
    }

    /**
     * Get the Downstream (Controller) OpenFlow version as dictated by the initial HELLO.
     *
     * @return OpenFlow version as dictated by the initial HELLO
     */
    public OFVersion getDownstreamVersion() {
        return downstreamVersion;
    }
}
