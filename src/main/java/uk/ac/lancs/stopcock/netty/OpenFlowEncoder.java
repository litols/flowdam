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

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import uk.ac.lancs.stopcock.openflow.Container;

import java.util.List;

/**
 * OpenFlowEncoder handles sending of Containers, which include a Header and raw data dump.
 */
class OpenFlowEncoder extends MessageToMessageEncoder<Container> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Container container, List<Object> objects) throws Exception {
        /* Construct a ByteBuf with the expected size of the output OpenFlow packet. */
        ByteBuf output = channelHandlerContext.alloc().buffer(container.getHeader().getLength());

        /* Write the binary data blob. */
        output.writeBytes(container.getData());

        /* Add back to the Netty pipeline. */
        objects.add(output);
    }
}
