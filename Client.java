package src;

import java.io.*;
import java.net.*;
import java.util.Random;

public class Client {
    private final int port;

    public Client(int port) {
        this.port = port;
    }

    public void discoverService() {
         InetAddress broadcastAddress = getBroadcastAddress();
        if (broadcastAddress == null){
            System.err.println("Broadcast address not found");
            return;
        }
        try (DatagramSocket udpSocket = new DatagramSocket()) {
            udpSocket.setBroadcast(true);

            String discoverMessage = "src.CCS DISCOVER";
            byte[] messageBytes = discoverMessage.getBytes();

            DatagramPacket packet = new DatagramPacket(
                    messageBytes,
                    messageBytes.length,
                    broadcastAddress,
                    port
            );
            System.out.println("Broadcasting discovery message...");
            udpSocket.send(packet);

            byte[] buffer = new byte[1024];
            DatagramPacket response = new DatagramPacket(buffer, buffer.length);
            udpSocket.receive(response);

            String responseMessage = new String(response.getData(), 0, response.getLength());
            if (responseMessage.startsWith("src.CCS FOUND")) {
                System.out.println("Service discovered at: " + response.getAddress());
                connectToServer(response.getAddress());
            } else {
                System.out.println("Unexpected response: " + responseMessage);
            }
        } catch (IOException e) {
            System.err.println("Error in service discovery: " + e.getMessage());
        }
    }
    private InetAddress getBroadcastAddress() {
        InetAddress localAddress = null;
        try {
            localAddress = Inet4Address.getLocalHost();
        } catch (UnknownHostException e) {
            System.err.println("Local address not found");
            return null;
        }
        NetworkInterface networkInterface = null;
        try {
            networkInterface = NetworkInterface.getByInetAddress(localAddress);
        } catch (SocketException e) {
            System.err.println("Network interface not found");
            return null;
        }
        for (InterfaceAddress address : networkInterface.getInterfaceAddresses()) {
            if (address.getAddress() instanceof Inet4Address) {
                InetAddress broadcast = address.getBroadcast();
                if (broadcast != null) return broadcast;
            }
        }
        return null;
    }

    public void connectToServer(InetAddress serverAddress) {
        try (Socket tcpSocket = new Socket(serverAddress, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));
             PrintWriter out = new PrintWriter(tcpSocket.getOutputStream(), true)) {

            System.out.println("Connected to server: " + serverAddress);

            Random random = new Random();
            String[] operations = {"ADD", "SUB", "MUL", "DIV", "DV", "AD", "ADD DD"};
            for (int i = 0; i < 50; i++) {
                String operation = operations[random.nextInt(operations.length)];
                int arg1 = random.nextInt(100);
                int arg2 = random.nextInt(100);

                String request = operation + " " + arg1 + " " + arg2;
                System.out.println("Sending request: " + request);
                out.println(request);

                String response = in.readLine();
                System.out.println("Received response: " + response);

                Thread.sleep(random.nextInt(500) + 500);
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("Error communicating with the server: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java src.Client <port>");
            return;
        }

        int port;
        try {
            port = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.out.println("Invalid port number: " + args[0]);
            return;
        }

        Client client = new Client(port);
        client.discoverService();
    }
}
