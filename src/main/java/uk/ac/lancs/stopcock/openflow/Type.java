/*
 * Stopcock - Copyright © 2014 - Lancaster University
 *
 * All rights reserved, unauthorised distribution of source code, compiled
 * binary or usage is expressly prohibited.
 */
package uk.ac.lancs.stopcock.openflow;

import uk.ac.lancs.stopcock.openflow.messages.OFPT;
import uk.ac.lancs.stopcock.openflow.messages.immutable.OFPTEchoReply;
import uk.ac.lancs.stopcock.openflow.messages.immutable.OFPTEchoRequest;
import uk.ac.lancs.stopcock.openflow.messages.switchconfiguration.OFPTFeaturesReply;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * Definitions of all different types of OpenFlow messages, their type numbers and direction the messages are
 * expected in.
 */
public enum Type {
    /* Immutable messages. */
    OFPT_HELLO(0, Direction.SYMMETRIC),
    OFPT_ERROR(1, Direction.SYMMETRIC),
    OFPT_ECHO_REQUEST(2, Direction.SYMMETRIC, OFPTEchoRequest.class),
    OFPT_ECHO_REPLY(3, Direction.SYMMETRIC, OFPTEchoReply.class),
    OFPT_EXPERIMENTER(4, Direction.SYMMETRIC),

    /* Switch configuration messages. */
    OFPT_FEATURES_REQUEST(5, Direction.CONTROLLER_SWITCH),
    OFPT_FEATURES_REPLY(6, Direction.CONTROLLER_SWITCH, OFPTFeaturesReply.class),
    OFPT_GET_CONFIG_REQUEST(7, Direction.CONTROLLER_SWITCH),
    OFPT_GET_CONFIG_REPLY(8, Direction.CONTROLLER_SWITCH),
    OFPT_SET_CONFIG(9, Direction.CONTROLLER_SWITCH),

    /* Asynchronous messages. */
    OFPT_PACKET_IN(10, Direction.ASYNCHRONOUS),
    OFPT_FLOW_REMOVED(11, Direction.ASYNCHRONOUS),
    OFPT_PORT_STATUS(12, Direction.ASYNCHRONOUS),

    /* Controller command messages. */
    OFPT_PACKET_OUT(13, Direction.CONTROLLER_SWITCH),
    OFPT_FLOW_MODE(14, Direction.CONTROLLER_SWITCH),
    OFPT_GROUP_MOD(15, Direction.CONTROLLER_SWITCH),
    OFPT_PORT_MOD(16, Direction.CONTROLLER_SWITCH),
    OFPT_TABLE_MOD(17, Direction.CONTROLLER_SWITCH),

    /* Mutlipart messages. */
    OFPT_MULTIPART_REQUEST(18, Direction.CONTROLLER_SWITCH),
    OFPT_MULTIPART_REPLY(19, Direction.CONTROLLER_SWITCH),

    /* Barrier messages. */
    OFPT_BARRIER_REQUEST(20, Direction.CONTROLLER_SWITCH),
    OFPT_BARRIER_REPLY(21, Direction.CONTROLLER_SWITCH),

    /* Queue Configuration messages. */
    OFPT_QUEUE_GET_CONFIG_REQUEST(22, Direction.CONTROLLER_SWITCH),
    OFPT_QUEUE_GET_CONFIG_REPLY(23, Direction.CONTROLLER_SWITCH),

    /* Controller role change request messages. */
    OFPT_ROLE_REQUEST(24, Direction.CONTROLLER_SWITCH),
    OFPT_ROLE_REPLY(25, Direction.CONTROLLER_SWITCH),

    /* Asynchronous message configuration. */
    OFPT_GET_ASYNC_REQUEST(26, Direction.CONTROLLER_SWITCH),
    OFPT_GET_ASYNC_REPLY(27, Direction.CONTROLLER_SWITCH),
    OFPT_SET_ASYNC(28, Direction.CONTROLLER_SWITCH),

    /* Meters and rate limiters configuration messages. */
    OFPT_METER_MOD(29, Direction.CONTROLLER_SWITCH);

    /** Internal cache of type number to Type object. */
    private static Map<Integer, Type> idMap = new HashMap<>();

    static {
        /* Fill internal cache of ids to objects. */
        for (Type type : Type.values()) {
            idMap.put(type.getId(), type);
        }
    }

    /**
     * Get an Type by the on wire protocol id.
     *
     * @param id type id to look up
     * @return Type represented by it, or null if not recognised
     */
    public static Type getById(int id) {
        return idMap.get(id);
    }

    /** Type ID */
    private int id;
    /** Direction of message expected. */
    private Direction direction;
    /** Class file representing packet. */
    private Class<? extends OFPT> klass = null;

    /**
     * Create a new Type object.
     *
     * @param id wire protocol type id
     * @param direction direction messages is expected
     * @param klass class name of internal representation, or null if none
     */
    Type(int id, Direction direction, Class<? extends OFPT> klass) {
        this.id = id;
        this.direction = direction;
        this.klass = klass;
    }

    /**
     * Create a new Type object.
     *
     * @param id wire protocol type id
     * @param direction direction messages is expected
     */
    Type(int id, Direction direction) {
        this(id, direction, null);
    }

    /**
     * Get the direction of transit the message is expected in.
     *
     * @return direction of message
     */
    public Direction getDirection() {
        return direction;
    }

    /**
     * Get the wire protocol type id.
     *
     * @return type id number
     */
    public int getId() {
        return id;
    }

    /**
     * Check to see if the Type can be turned into a OFPT object.
     *
     * @return true if the Type can convert a payload into an OFPT object
     */
    public boolean canCreateInstance() {
        return (klass != null);
    }

    /**
     * Parse the provided payload and produce an OFPT descended Object which represents the Type/Payload.
     *
     * @param payload payload to parse
     * @return OFPT descended Object, or null if it couldn't be instantiated
     */
    public OFPT parseTypePayload(byte[] payload) {
        /* If there's no payload or we have no klass we can't work. */
        if (payload == null || klass == null) {
            return null;
        }

        /* Attempt to construct the object. */
        try {
            Constructor<?> cons = klass.getConstructor(byte[].class);
            return (OFPT) cons.newInstance(payload);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }
}
