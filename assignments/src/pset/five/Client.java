package pset.five;

import java.io.*;
import java.net.*;
import java.util.Scanner;

/** Client class
 */
public class Client {

    private static DataInputStream stdin;
    private static DataOutputStream stdout;

    /** Start a client based on given command line arguments.
     */
    @SuppressWarnings("resource")
    public static void main (String[] args) {

        String hostAddress;
        int tcpPort;
        int udpPort;

        if (args.length != 3 && args.length != 4) {
            String s = "";
            s += ("ERROR: Provide 3 arguments\n");
            s += ("\t(1) <hostAddress>: the address of the server\n");
            s += ("\t(2) <tcpPort>: the port number for TCP connection\n");
            s += ("\t(3) <udpPort>: the port number for UDP connection\n");
            s += ("\t<4> <input>: plaintext file containing input commands\n");
            System.out.println(s);
            System.exit(-1);
        }

        hostAddress = args[0];
        tcpPort = Integer.parseInt(args[1]);
        udpPort = Integer.parseInt(args[2]);

        Scanner sc = null;
        if (args.length == 3) {
            sc = new Scanner(System.in);
        } else {
            try {
                sc = new Scanner(new File(args[3]));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        while(sc.hasNextLine()) {

            String cmd = sc.nextLine();
            String[] tokens = cmd.split("\\s+");

            if (tokens[0].equals("purchase")) {
            } else if (tokens[0].equals("cancel")) {
            } else if (tokens[0].equals("search")) {
            } else if (tokens[0].equals("list")) {
            } else {
                System.out.println("ERROR: No such command");
            }

            boolean udp = tokens[tokens.length-1].toLowerCase().startsWith("u");
            String message = String.format("%s\n", cmd);
            String response = "";
            try {
                if (udp) {
                    response = sendUdp(message, hostAddress, udpPort);
                } else {
                    response = sendTcp(message, hostAddress, tcpPort);
                }
            } catch (IOException e) { e.printStackTrace(); }
            System.out.print(String.format("%s", response));
        }
    }

    /** Send a message to specified address and port via UDP.
     */
    public static String sendUdp(String message, String address, int port)
        throws IOException {

        byte[] sendData = message.getBytes();
        @SuppressWarnings("resource")
            DatagramSocket dsocket = new DatagramSocket();
        InetAddress iAddress = InetAddress.getByName(address);
        DatagramPacket sendPacket = new DatagramPacket(sendData,
                                                       sendData.length,
                                                       iAddress, port);
        dsocket.send(sendPacket);

        byte[] receiveData = new byte[2048];
        DatagramPacket receivePacket = new DatagramPacket(receiveData,
                                                          receiveData.length);
        dsocket.receive(receivePacket);
        return new String(receivePacket.getData());
    }

    /** Send a message to specified address and port via TCP.
     */
    public static String sendTcp(String message, String address, int port)
        throws IOException {
      
        Socket ssocket = new Socket(address, port);
        stdout = new DataOutputStream(ssocket.getOutputStream());
        stdin = new DataInputStream(ssocket.getInputStream());
        stdout.writeUTF(message);
        return stdin.readUTF();
    }
}
