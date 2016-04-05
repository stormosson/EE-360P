package pset.five;

import java.util.Scanner;

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ListIterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


/* TODO: rename this to something sensible; it sends messages too */
/**
 * Listener class to accept requests over a TCP connection.
 */
class TcpListener implements Runnable {

    int port;
    String address;
    Server server = null;
    ServerSocket ssocket;
    private final Integer BACKLOG = 100;

    private static int getPortFromAddress(String str) {
        return Integer.parseInt(str.split(":")[1]);
    }

    private static String getHostFromAddress(String str) {
        return str.split(":")[0];
    }

    /**
     * Spawn a TCP listener on specified port.
     */
    TcpListener(String address) {
        this(getHostFromAddress(address), getPortFromAddress(port));
    }

    /**
     * Spawn a TCP listener on specified port.
     */
    TcpListener(String address, int port) {
        this(address, port, null);
    }

    /**
     * Spawn a TCP listener on specified port.
     */
    TcpListener(String address, int port, Server server) {
        this.port = port;
        this.address = address;
        this.server = server;

        try {
            InetAddress addr = InetAddress.getByName(address);
            this.ssocket = new ServerSocket(port, BACKLOG, addr);
        } catch (IOException e) {
            System.err.format("Server aborted: %s", e);
        }
    }

    /* TODO: implement */
    public void sendMessage(Message msg) {
        
    }

    /**
     * Listener event loop -- dispatches messages to Handler.
     */
    @Override
    public void run() {
        try {
            while (true) {
                /* dispatch an anonymous Handler thread */
                new Thread(new Handler(ssocket.accept(), server)).start();
            }
        } catch (IOException e) {
            System.err.format("Server aborted: %s", e);
        }
    }
}


/** Handler class to dispatch received commands to the singleton Server.
 */
class Handler implements Runnable {

    String[] command;
    Server server;
    Socket tcpsocket;

    /** Create a handler capable of responding over TCP. */
    public Handler(Socket tcpsocket, Server server) {
        this.tcpsocket = tcpsocket;
        this.server = server;
        DataInputStream stdin;
        try {
            stdin = new DataInputStream(tcpsocket.getInputStream());
            String cmd = stdin.readUTF();
            this.command = cmd.trim().split("\\s+", 2);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Handler's run method, parses received command and relays information to
     * Server.
     */
    @Override
    public void run() {
        try {
            String response = "";
            ArrayList<String> parameters = new 
                ArrayList<String>(Arrays.asList(command[1].split("\\s+")));

            Message message = new Message(command[0], parameters);
            String responseToClient = server.enqueue(msg);
            respond(String.format("%s\n", responseToClient.trim()));
        } catch (IOException e) {
            System.err.format("Request aborted: %s", e);
        }
    }

    /** Respond via TCP to the Client that pinged the Server API. */
    private void respond(String message) throws IOException {
        DataOutputStream stdout =
            new DataOutputStream(tcpsocket.getOutputStream());
        stdout.writeUTF(message);
    }
}
