package pset.three;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Client {

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

        Scanner sc = new Scanner((args.length == 3 ? System.in : args[3]));
        while(sc.hasNextLine()) {

            String cmd = sc.nextLine();
            String[] tokens = cmd.split("\\s+");

            /* Honestly, what the hell is this */
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
            if (udp) {
                response = sendUcp(message, udpPort);
            } else {
                response = sendTcp(message, tcpPort);
            }
            System.out.println(response);
        }
    }

    public String sendUdp(String message, int port) {

        byte[] sendData = message.getBytes();
        DatagramSocket dsocket = new DatagramSocket();
        InetAddress address = InetAddress.getByName("localhost");
        DatagramPacket sendPacket = new DatagramPacket(sendData, 
                                                       sendData.length,
                                                       address, port);
        ssocket.send(sendPacket);

        byte[] receiveData = new byte[2048];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, 
                                                          receiveData.length);
        ssocket.receive(receivePacket);
        return new String(receivePacket.getData());
    }

    public String sendTcp(String message, int port) {
        
        Socket ssocket = new Socket("localhost", port);
        DataOutputStream stdout = 
            new DataOutputStream(ssocket.getOutputStream());
        BufferedReader stdin = 
            new BufferedReader(new InputStreamReader(ssocket.getInputStream()));

        stdout.writeBytes(message);
        return stdin.readLine();
    }
}
