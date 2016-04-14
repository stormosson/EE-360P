package pset.five;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

/** Reduce code duplication by providing a common library for internet address
 * manipulation. */
class InetManipulator {
    public static int getPortFromAddress(String str) {
        return Integer.parseInt(str.split(":")[1]);
    }

    public static String getHostFromAddress(String str) {
        return str.split(":")[0];
    }
}


/**
 * Listener class to accept requests over a TCP connection.
 */
class TcpListener implements Runnable {

    int port;
    String address;
    Server server = null;
    ServerSocket ssocket;
    private final Integer BACKLOG = 100;

    /**
     * Spawn a TCP listener on specified port.
     */
    TcpListener(String address) {
        this(InetManipulator.getHostFromAddress(address), 
             InetManipulator.getPortFromAddress(address));
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

    /** Send a message to this TcpListener's corresponding Server. */
    /* I have an inkling that this is going to need to dispatch the
     * command contained inside msg to a Handler, such that this thread doesn't
     * block or not all servers will be notified in parallel by
     * notifyServers. With the current setup we're getting serial
     * notifications, sounds ripe for a deadlock */
    public void sendMessage(Message msg) {
        //must use enqueue
    	server.enqueue(msg);
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
            String responseToClient = server.enqueue(message);
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
