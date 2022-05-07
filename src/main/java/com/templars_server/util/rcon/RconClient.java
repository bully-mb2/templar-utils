package com.templars_server.util.rcon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.net.*;
import java.nio.charset.Charset;

public class RconClient {

    private static final Logger LOG = LoggerFactory.getLogger(RconClient.class);
    private static final String COMMAND_PREFIX = new String(new char[]{255, 255, 255, 255});
    private static final Charset CHARSET = Charset.forName("cp1252");
    private static final int RECEIVE_BUFFER_SIZE = 1024;
    private static final int DEFAULT_TIMEOUT_MILLISECONDS = 200;

    private InetSocketAddress address;
    private String password;
    private DatagramSocket socket;

    public void connect(InetSocketAddress address, String password) throws SocketException {
        connect(
                address,
                new InetSocketAddress(0),
                password,
                DEFAULT_TIMEOUT_MILLISECONDS
        );
    }

    public void connect(InetSocketAddress address, InetSocketAddress bindAddress, String password, int timeoutMilliseconds) throws SocketException {
        this.address = address;
        this.password = password;

        LOG.info(String.format("Binding RconClient to %s", bindAddress.getHostString()));
        socket = new DatagramSocket(bindAddress);
        socket.setSoTimeout(timeoutMilliseconds);
        LOG.info("Ready to send commands");

    }

    @SuppressWarnings("InfiniteLoopStatement")
    public String send(String command) throws IOException {
        String payload = String.format("%srcon %s %s", COMMAND_PREFIX, password, command);
        byte[] bytes = payload.getBytes(CHARSET);
        DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address);
        socket.send(packet);

        StringWriter response = new StringWriter();
        try {
            while (true) {
                bytes = new byte[RECEIVE_BUFFER_SIZE];
                packet = new DatagramPacket(bytes, bytes.length);
                socket.receive(packet);
                String unfiltered = new String(bytes, CHARSET);
                response.append(unfiltered.replaceAll("\\x00", "").replace(COMMAND_PREFIX + "print\n", ""));
            }
        } catch (SocketTimeoutException ignored) {
        }

        return response.toString();
    }

}
