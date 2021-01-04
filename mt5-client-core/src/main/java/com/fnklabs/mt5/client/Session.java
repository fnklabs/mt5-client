package com.fnklabs.mt5.client;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicInteger;

class Session implements AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(Session.class);

    private final SocketChannel socket;
    private final AtomicInteger seqCounter = new AtomicInteger(0);

    Session(String host, int port) throws IOException {
        socket = SocketChannel.open(new InetSocketAddress(host, port));
    }

    public int nextId() {
        return seqCounter.incrementAndGet();
    }

    public SocketChannel getSocket() {
        return socket;
    }

    @Override
    public void close() throws Exception {
        socket.close();
    }

    /**
     * Read packet from socket and return response body without header
     *
     * @return response body
     *
     * @throws RequestExecutionException on read exception
     */
    public byte[] read() throws RequestExecutionException {
        try {
            ByteBuffer headBuf = ByteBuffer.allocate(RawApiMarshaller.HEADER_SIZE);

            while (headBuf.remaining() != 0) { // read header
                getSocket().read(headBuf);
            }

            log.debug("Header: {}/{}", new String(headBuf.array()), headBuf.array());

            byte[] msgSizeBytes = ArrayUtils.subarray(headBuf.array(), 0, 4);
            String msg = new String(msgSizeBytes);
            int msgSize = Integer.parseInt(msg, 16);

            log.debug("header msg size: {}/{} msg size: {}", msg, Hex.encodeHexString(msgSizeBytes), msgSize);

            ByteBuffer responseBuf = ByteBuffer.allocate(msgSize);
            while (responseBuf.remaining() != 0) { // read header
                getSocket().read(responseBuf);
            }

            responseBuf.flip();

            log.debug("read {} bytes. hex data: {}", responseBuf.limit(), Hex.encodeHexString(responseBuf.array()));

            byte[] data = new byte[responseBuf.limit()];

            responseBuf.get(data);

            return data;

        } catch (Exception e) {
            throw new RequestExecutionException(e);
        }

    }

    /**
     * Write command to session
     *
     * @param packet MT5 packet
     *
     * @throws RequestExecutionException on send error
     */
    public void write(byte[] packet) throws RequestExecutionException {
        try {
            socket.write(ByteBuffer.wrap(packet));
        } catch (Exception e) {
            throw new RequestExecutionException(e);
        }

    }
}
