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
package com.leafgraph.flowdam.openflow;

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
    OFPT_ECHO_REQUEST(2, Direction.SYMMETRIC),
    OFPT_ECHO_REPLY(3, Direction.SYMMETRIC),
    OFPT_EXPERIMENTER(4, Direction.SYMMETRIC),

    /* Switch configuration messages. */
    OFPT_FEATURES_REQUEST(5, Direction.CONTROLLER_SWITCH),
    OFPT_FEATURES_REPLY(6, Direction.CONTROLLER_SWITCH),
    OFPT_GET_CONFIG_REQUEST(7, Direction.CONTROLLER_SWITCH),
    OFPT_GET_CONFIG_REPLY(8, Direction.CONTROLLER_SWITCH),
    OFPT_SET_CONFIG(9, Direction.CONTROLLER_SWITCH),

    /* Asynchronous messages. */
    OFPT_PACKET_IN(10, Direction.ASYNCHRONOUS),
    OFPT_FLOW_REMOVED(11, Direction.ASYNCHRONOUS),
    OFPT_PORT_STATUS(12, Direction.ASYNCHRONOUS),

    /* Controller command messages. */
    OFPT_PACKET_OUT(13, Direction.CONTROLLER_SWITCH),
    OFPT_FLOW_MOD(14, Direction.CONTROLLER_SWITCH),
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

    /**
     * Internal cache of type number to Type object.
     */
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

    /**
     * Type ID
     */
    private int id;
    /**
     * Direction of message expected.
     */
    private Direction direction;

    /**
     * Create a new Type object.
     *
     * @param id        wire protocol type id
     * @param direction direction messages is expected
     */
    Type(int id, Direction direction) {
        this.id = id;
        this.direction = direction;
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
}
