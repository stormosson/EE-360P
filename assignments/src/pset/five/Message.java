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

enum MessageType {
    /* Only the Client (NONE) option is external, i.e. unrelated to
     * synchronization. */
    NONE,                       /* when unspecified, message is from Client */
    REQUEST,
    RELEASE,
    ACK;

    private int type;
    public int type() { return type; }
}

class ServerCommand {
    private String command;
    private ArrayList<String> parameters;

    ServerCommand(String cmd, ArrayList<String> params) {
        this.command = cmd;
        this.parameters = params;
    }
    public String getCommand() { return this.command; }
    public ArrayList<String> getParameters() { return this.parameters; }
}

/** Class for creating messages used in Lamport's Algorithm. */
class Message implements Comparable<Message> {

    private ServerCommand = server_command;
    private Server sender;
    private Timestamp timestamp;
    private MessageType type;

    public Message(Server sender, ServerCommand srv_cmd, Timestamp timestamp) {
        this.sender = sender;
        this.server_command = srv_cmd;
        this.timestamp = timestamp;
        this.type = MessageType.NONE;
    }

    public Message(Server sender, ServerCommand srv_cmd, Timestamp timestamp,
                   MessageType type) {
        this.sender = sender;
        this.server_command = srv_cmd;
        this.timestamp = timestamp;
        this.type = type;
    }

    public Message(Server sender, Timestamp timestamp, MessageType type) {
        this.sender = sender;
        this.timestamp = timestamp;
        this.message = String.new();
        this.type = type;
    }

    public ServerCommand getServerCommand() {
        return server_command;
    }

    public MessageType type() {
        return type;
    }

    public Server getSender() { return sender; }

    public Timestamp getTimestamp() { return timestamp; }

    public String getMessageString(){
        return message;
    }

    public void incrementTimestamp(){
        timestamp.increment();
    }

    @Override
    public int compareTo(final Message that) {
        return this.timestamp.compareTo(that.getTimestamp());
    }
}
