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
    public String send(String command) {
        String payload = String.format("%srcon %s %s", COMMAND_PREFIX, password, command);
        byte[] bytes = payload.getBytes(CHARSET);
        DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address);
        StringWriter response = new StringWriter();

        try {
            socket.send(packet);
            while (true) {
                bytes = new byte[RECEIVE_BUFFER_SIZE];
                packet = new DatagramPacket(bytes, bytes.length);
                socket.receive(packet);
                String unfiltered = new String(bytes, CHARSET);
                response.append(unfiltered.replaceAll("\\x00", "").replace(COMMAND_PREFIX + "print\n", ""));
            }
        } catch (SocketTimeoutException ignored) {
        } catch (IOException e) {
            LOG.error("Couldn't send rcon command", e);
        }

        return response.toString();
    }

    public String addIp(String ip) {
        return send(String.format("addip \"%s\"", ip));
    }

    public String removeIp(String ip) {
        return send(String.format("removeip \"%s\"", ip));
    }

    public String kick(int slot) {
        return send(String.format("kick %d", slot));
    }

    public String ban(int slot) {
        return send(String.format("ban %d", slot));
    }

    public String status() {
        return send("status");
    }

    public String say(String message) {
        return send(String.format("svsay \"%s\"", message));
    }

    public String tell(int slot, String message) {
        return send(String.format("svtell %d \"%s\"", slot, message));
    }

    public String newRound() {
        return send("newround");
    }

    public String map(String map) {
        return send("map \"%s\"");
    }

    public String mode(int mode) {
        return send(String.format("mbmode %d", mode));
    }

    public String mode(int mode, String map) {
        return send("mbmode %d \"%s\"");
    }

    public String printAll(String message) {
        return print("all", message, false);
    }

    public String print(int slot, String message) {
        return print("" + slot, message, false);
    }

    public String printConAll(String message) {
        return print("all", message, true);
    }

    public String printCon(int slot, String message) {
        return print("" + slot, message, true);
    }

    private String print(String target, String message, boolean consoleOnly) {
        if (consoleOnly) {
            return send(String.format("svprintcon %s \"%s\"", target, message));
        }

        return send(String.format("svprint %s \"%s\"", target, message));
    }

}
