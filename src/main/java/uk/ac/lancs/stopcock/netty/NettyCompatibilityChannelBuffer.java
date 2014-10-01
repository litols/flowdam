/*
 * Stopcock - Copyright © 2014 - Lancaster University
 *
 * All rights reserved, unauthorised distribution of source code, compiled
 * binary or usage is expressly prohibited.
 */
package uk.ac.lancs.stopcock.netty;

import io.netty.buffer.ByteBuf;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferFactory;
import org.jboss.netty.buffer.ChannelBufferIndexFinder;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.charset.Charset;

/**
 * The NettyCompatibilityChannelBuffer provides an interoperability layer between openflowj and Netty 4, this is
 * required because openflowj uses netty 3.X and thus the org.jboss package space, this layer implements that but
 * backing off the a Netty 4.0 ByteBuf.
 *
 * Most basic functions have been proxied, any functions involving ChannelBuffers or further ByteBuf's have been
 * implemented to throw NotImplementedException. Basic read operation in openflowj does not seem to all anything
 * but the basic methods.
 */
public class NettyCompatibilityChannelBuffer implements ChannelBuffer {
    /** Netty 4 ByteBuf to proxy to. */
    private ByteBuf byteBuf;

    /**
     * Constructs a new NettyCompatibilityChannelBuffer backed by a Netty 4 ByteBuf, read class javadoc for caveats.
     *
     * @param byteBuf Netty 4 ByteBuf
     */
    public NettyCompatibilityChannelBuffer(ByteBuf byteBuf) {
        this.byteBuf = byteBuf;
    }

    @Override
    public ChannelBufferFactory factory() {
        return null;
    }

    @Override
    public int capacity() {
        return byteBuf.capacity();
    }

    @Override
    public ByteOrder order() {
        return byteBuf.order();
    }

    @Override
    public boolean isDirect() {
        return byteBuf.isDirect();
    }

    @Override
    public int readerIndex() {
        return byteBuf.readerIndex();
    }

    @Override
    public void readerIndex(int i) {
        byteBuf.readerIndex(i);
    }

    @Override
    public int writerIndex() {
        return byteBuf.writerIndex();
    }

    @Override
    public void writerIndex(int i) {
        byteBuf.writerIndex(i);
    }

    @Override
    public void setIndex(int i, int i2) {
        byteBuf.setIndex(i, i2);
    }

    @Override
    public int readableBytes() {
        return byteBuf.readableBytes();
    }

    @Override
    public int writableBytes() {
        return byteBuf.writableBytes();
    }

    @Override
    public boolean readable() {
        return byteBuf.isReadable();
    }

    @Override
    public boolean writable() {
        return byteBuf.isWritable();
    }

    @Override
    public void clear() {
        byteBuf.clear();
    }

    @Override
    public void markReaderIndex() {
        byteBuf.markReaderIndex();
    }

    @Override
    public void resetReaderIndex() {
        byteBuf.resetReaderIndex();
    }

    @Override
    public void markWriterIndex() {
        byteBuf.markWriterIndex();
    }

    @Override
    public void resetWriterIndex() {
        byteBuf.resetWriterIndex();
    }

    @Override
    public void discardReadBytes() {
        byteBuf.discardReadBytes();
    }

    @Override
    public void ensureWritableBytes(int i) {
        byteBuf.ensureWritable(i);
    }

    @Override
    public byte getByte(int i) {
        return byteBuf.getByte(i);
    }

    @Override
    public short getUnsignedByte(int i) {
        return byteBuf.getUnsignedByte(i);
    }

    @Override
    public short getShort(int i) {
        return byteBuf.getShort(i);
    }

    @Override
    public int getUnsignedShort(int i) {
        return byteBuf.getUnsignedShort(i);
    }

    @Override
    public int getMedium(int i) {
        return byteBuf.getMedium(i);
    }

    @Override
    public int getUnsignedMedium(int i) {
        return byteBuf.getUnsignedMedium(i);
    }

    @Override
    public int getInt(int i) {
        return byteBuf.getInt(i);
    }

    @Override
    public long getUnsignedInt(int i) {
        return byteBuf.getUnsignedInt(i);
    }

    @Override
    public long getLong(int i) {
        return byteBuf.getLong(i);
    }

    @Override
    public char getChar(int i) {
        return byteBuf.getChar(i);
    }

    @Override
    public float getFloat(int i) {
        return byteBuf.getFloat(i);
    }

    @Override
    public double getDouble(int i) {
        return byteBuf.getDouble(i);
    }

    @Override
    public void getBytes(int i, ChannelBuffer channelBuffer) {
        throw new NotImplementedException();
    }

    @Override
    public void getBytes(int i, ChannelBuffer channelBuffer, int i2) {
        throw new NotImplementedException();
    }

    @Override
    public void getBytes(int i, ChannelBuffer channelBuffer, int i2, int i3) {
        throw new NotImplementedException();
    }

    @Override
    public void getBytes(int i, byte[] bytes) {
        byteBuf.getBytes(i, bytes);
    }

    @Override
    public void getBytes(int i, byte[] bytes, int i2, int i3) {
        byteBuf.getBytes(i, bytes, i2, i3);
    }

    @Override
    public void getBytes(int i, ByteBuffer byteBuffer) {
        byteBuf.getBytes(i, byteBuffer);
    }

    @Override
    public void getBytes(int i, OutputStream outputStream, int i2) throws IOException {
        byteBuf.getBytes(i, outputStream, i2);
    }

    @Override
    public int getBytes(int i, GatheringByteChannel gatheringByteChannel, int i2) throws IOException {
        return byteBuf.getBytes(i, gatheringByteChannel, i2);
    }

    @Override
    public void setByte(int i, int i2) {
        byteBuf.setByte(i, i2);
    }

    @Override
    public void setShort(int i, int i2) {
        byteBuf.setShort(i, i2);
    }

    @Override
    public void setMedium(int i, int i2) {
        byteBuf.setMedium(i, i2);
    }

    @Override
    public void setInt(int i, int i2) {
        byteBuf.setInt(i, i2);
    }

    @Override
    public void setLong(int i, long l) {
        byteBuf.setLong(i, l);
    }

    @Override
    public void setChar(int i, int i2) {
        byteBuf.setChar(i, i2);
    }

    @Override
    public void setFloat(int i, float v) {
        byteBuf.setFloat(i, v);
    }

    @Override
    public void setDouble(int i, double v) {
        byteBuf.setDouble(i, v);
    }

    @Override
    public void setBytes(int i, ChannelBuffer channelBuffer) {
        throw new NotImplementedException();
    }

    @Override
    public void setBytes(int i, ChannelBuffer channelBuffer, int i2) {
        throw new NotImplementedException();
    }

    @Override
    public void setBytes(int i, ChannelBuffer channelBuffer, int i2, int i3) {
        throw new NotImplementedException();
    }

    @Override
    public void setBytes(int i, byte[] bytes) {
        byteBuf.setBytes(i, bytes);
    }

    @Override
    public void setBytes(int i, byte[] bytes, int i2, int i3) {
        byteBuf.setBytes(i, bytes, i2, i3);
    }

    @Override
    public void setBytes(int i, ByteBuffer byteBuffer) {
        throw new NotImplementedException();
    }

    @Override
    public int setBytes(int i, InputStream inputStream, int i2) throws IOException {
        return byteBuf.setBytes(i, inputStream, i2);
    }

    @Override
    public int setBytes(int i, ScatteringByteChannel scatteringByteChannel, int i2) throws IOException {
        return byteBuf.setBytes(i, scatteringByteChannel, i2);
    }

    @Override
    public void setZero(int i, int i2) {
        byteBuf.setZero(i, i2);
    }

    @Override
    public byte readByte() {
        return byteBuf.readByte();
    }

    @Override
    public short readUnsignedByte() {
        return byteBuf.readUnsignedByte();
    }

    @Override
    public short readShort() {
        return byteBuf.readShort();
    }

    @Override
    public int readUnsignedShort() {
        return byteBuf.readUnsignedShort();
    }

    @Override
    public int readMedium() {
        return byteBuf.readMedium();
    }

    @Override
    public int readUnsignedMedium() {
        return byteBuf.readUnsignedMedium();
    }

    @Override
    public int readInt() {
        return byteBuf.readInt();
    }

    @Override
    public long readUnsignedInt() {
        return byteBuf.readUnsignedInt();
    }

    @Override
    public long readLong() {
        return byteBuf.readLong();
    }

    @Override
    public char readChar() {
        return byteBuf.readChar();
    }

    @Override
    public float readFloat() {
        return byteBuf.readFloat();
    }

    @Override
    public double readDouble() {
        return byteBuf.readDouble();
    }

    @Override
    public ChannelBuffer readBytes(int i) {
        throw new NotImplementedException();
    }

    @Override
    public ChannelBuffer readBytes(ChannelBufferIndexFinder channelBufferIndexFinder) {
        throw new NotImplementedException();
    }

    @Override
    public ChannelBuffer readSlice(int i) {
        throw new NotImplementedException();
    }

    @Override
    public ChannelBuffer readSlice(ChannelBufferIndexFinder channelBufferIndexFinder) {
        throw new NotImplementedException();
    }

    @Override
    public void readBytes(ChannelBuffer channelBuffer) {
        throw new NotImplementedException();
    }

    @Override
    public void readBytes(ChannelBuffer channelBuffer, int i) {
        throw new NotImplementedException();
    }

    @Override
    public void readBytes(ChannelBuffer channelBuffer, int i, int i2) {
        throw new NotImplementedException();
    }

    @Override
    public void readBytes(byte[] bytes) {
        byteBuf.readBytes(bytes);
    }

    @Override
    public void readBytes(byte[] bytes, int i, int i2) {
        byteBuf.readBytes(bytes, i, i2);
    }

    @Override
    public void readBytes(ByteBuffer byteBuffer) {
        throw new NotImplementedException();
    }

    @Override
    public void readBytes(OutputStream outputStream, int i) throws IOException {
        byteBuf.readBytes(outputStream, i);
    }

    @Override
    public int readBytes(GatheringByteChannel gatheringByteChannel, int i) throws IOException {
        return byteBuf.readBytes(gatheringByteChannel, i);
    }

    @Override
    public void skipBytes(int i) {
        byteBuf.skipBytes(i);
    }

    @Override
    public int skipBytes(ChannelBufferIndexFinder channelBufferIndexFinder) {
        throw new NotImplementedException();
    }

    @Override
    public void writeByte(int i) {
        byteBuf.writeByte(i);
    }

    @Override
    public void writeShort(int i) {
        byteBuf.writeShort(i);
    }

    @Override
    public void writeMedium(int i) {
        byteBuf.writeMedium(i);
    }

    @Override
    public void writeInt(int i) {
        byteBuf.writeInt(i);
    }

    @Override
    public void writeLong(long l) {
        byteBuf.writeLong(l);
    }

    @Override
    public void writeChar(int i) {
        byteBuf.writeChar(i);
    }

    @Override
    public void writeFloat(float v) {
        byteBuf.writeFloat(v);
    }

    @Override
    public void writeDouble(double v) {
        byteBuf.writeDouble(v);
    }

    @Override
    public void writeBytes(ChannelBuffer channelBuffer) {
        throw new NotImplementedException();
    }

    @Override
    public void writeBytes(ChannelBuffer channelBuffer, int i) {
        throw new NotImplementedException();
    }

    @Override
    public void writeBytes(ChannelBuffer channelBuffer, int i, int i2) {
        throw new NotImplementedException();
    }

    @Override
    public void writeBytes(byte[] bytes) {
        byteBuf.writeBytes(bytes);
    }

    @Override
    public void writeBytes(byte[] bytes, int i, int i2) {
        byteBuf.writeBytes(bytes, i, i2);
    }

    @Override
    public void writeBytes(ByteBuffer byteBuffer) {
        throw new NotImplementedException();
    }

    @Override
    public int writeBytes(InputStream inputStream, int i) throws IOException {
        return byteBuf.writeBytes(inputStream, i);
    }

    @Override
    public int writeBytes(ScatteringByteChannel scatteringByteChannel, int i) throws IOException {
        return byteBuf.writeBytes(scatteringByteChannel, i);
    }

    @Override
    public void writeZero(int i) {
        byteBuf.writeZero(i);
    }

    @Override
    public int indexOf(int i, int i2, byte b) {
        return byteBuf.indexOf(i, i2, b);
    }

    @Override
    public int indexOf(int i, int i2, ChannelBufferIndexFinder channelBufferIndexFinder) {
        throw new NotImplementedException();
    }

    @Override
    public int bytesBefore(byte b) {
        return byteBuf.bytesBefore(b);
    }

    @Override
    public int bytesBefore(ChannelBufferIndexFinder channelBufferIndexFinder) {
        throw new NotImplementedException();
    }

    @Override
    public int bytesBefore(int i, byte b) {
        return byteBuf.bytesBefore(i, b);
    }

    @Override
    public int bytesBefore(int i, ChannelBufferIndexFinder channelBufferIndexFinder) {
        throw new NotImplementedException();
    }

    @Override
    public int bytesBefore(int i, int i2, byte b) {
        return byteBuf.bytesBefore(i, i2, b);
    }

    @Override
    public int bytesBefore(int i, int i2, ChannelBufferIndexFinder channelBufferIndexFinder) {
        throw new NotImplementedException();
    }

    @Override
    public ChannelBuffer copy() {
        throw new NotImplementedException();
    }

    @Override
    public ChannelBuffer copy(int i, int i2) {
        throw new NotImplementedException();
    }

    @Override
    public ChannelBuffer slice() {
        throw new NotImplementedException();
    }

    @Override
    public ChannelBuffer slice(int i, int i2) {
        throw new NotImplementedException();
    }

    @Override
    public ChannelBuffer duplicate() {
        throw new NotImplementedException();
    }

    @Override
    public ByteBuffer toByteBuffer() {
        throw new NotImplementedException();
    }

    @Override
    public ByteBuffer toByteBuffer(int i, int i2) {
        throw new NotImplementedException();
    }

    @Override
    public ByteBuffer[] toByteBuffers() {
        throw new NotImplementedException();
    }

    @Override
    public ByteBuffer[] toByteBuffers(int i, int i2) {
        throw new NotImplementedException();
    }

    @Override
    public boolean hasArray() {
        return byteBuf.hasArray();
    }

    @Override
    public byte[] array() {
        return byteBuf.array();
    }

    @Override
    public int arrayOffset() {
        return byteBuf.arrayOffset();
    }

    @Override
    public String toString(Charset charset) {
        return byteBuf.toString(charset);
    }

    @Override
    public String toString(int i, int i2, Charset charset) {
        return byteBuf.toString(i, i2, charset);
    }

    @Override
    public String toString(String s) {
        throw new NotImplementedException();
    }

    @Override
    public String toString(String s, ChannelBufferIndexFinder channelBufferIndexFinder) {
        throw new NotImplementedException();
    }

    @Override
    public String toString(int i, int i2, String s) {
        throw new NotImplementedException();
    }

    @Override
    public String toString(int i, int i2, String s, ChannelBufferIndexFinder channelBufferIndexFinder) {
        throw new NotImplementedException();
    }

    @Override
    public int compareTo(ChannelBuffer channelBuffer) {
        throw new NotImplementedException();
    }
}
