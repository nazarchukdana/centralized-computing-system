package src;

import java.io.*;
import java.net.*;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.*;
enum Stats{
    ADD, SUB, MUL, DIV, CONNECTED_CLIENTS, COMPUTED_REQUESTS, SUM_OF_RESULTS, INCORRECT_OPERATIONS

}

public class CCS {
    private final int port;
    private final Map<Stats, Integer> globalStats;
    private final ScheduledExecutorService scheduler;
    private final Map<Stats, Integer> lastStats;
    private final ExecutorService clientHandlerPool = Executors.newCachedThreadPool();

    public CCS(int port) {
        this.port = port;
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.globalStats = new EnumMap<>(Stats.class);
        this.lastStats = new EnumMap<>(Stats.class);
        for (Stats stat : Stats.values()) {
            globalStats.put(stat, 0);
            lastStats.put(stat, 0);
        }
    }

    public void start() {
        Thread udpListenerThread = new Thread(this::startUDPListener);
        udpListenerThread.start();
        Thread tcpListenerThread = new Thread(this::startTCPServer);
        tcpListenerThread.start();
        scheduler.scheduleAtFixedRate(this::reportStatistics, 10, 10, TimeUnit.SECONDS);
    }

    private void startUDPListener() {
        try (DatagramSocket udpSocket = new DatagramSocket(port)) {
            byte[] buffer = new byte[1024];
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                udpSocket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());
                if (message.startsWith("src.CCS DISCOVER")) {
                    System.out.println("Service discovery request received.");
                    byte[] response = "src.CCS FOUND".getBytes();
                    DatagramPacket responsePacket = new DatagramPacket(response, response.length, packet.getAddress(), packet.getPort());
                    udpSocket.send(responsePacket);
                    System.out.println("Service discovery response sent.");
                }
            }
        } catch (IOException e) {
            System.err.println("Error in UDP listener: " + e.getMessage());
            System.exit(1);
        }
    }

    private void startTCPServer() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("TCP server listening on port " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                incrementStats(Stats.CONNECTED_CLIENTS);
                System.out.println("src.Client connected: " + clientSocket.getInetAddress());
                clientHandlerPool.execute(() -> handleClient(clientSocket));
            }
        } catch (IOException e) {
            System.err.println("Error in TCP server: " + e.getMessage());
        }
    }

    private void handleClient(Socket clientSocket) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            String request;
            while ((request = in.readLine()) != null) {
                System.out.println("Request received: " + request);
                String[] parts = request.split(" ");
                if (parts.length != 3) {
                    incrementStats(Stats.INCORRECT_OPERATIONS);
                    out.println("ERROR");
                    continue;
                }
                String operation = parts[0];
                int result;
                try {
                    int arg1 = Integer.parseInt(parts[1]);
                    int arg2 = Integer.parseInt(parts[2]);
                    result = performOperation(operation, arg1, arg2);
                    incrementStats(Stats.COMPUTED_REQUESTS);
                    changeStats(Stats.SUM_OF_RESULTS, result);
                    out.println(result);
                    System.out.println("Result sent: " + result);
                } catch (ArithmeticException | IllegalArgumentException e ) {
                    incrementStats(Stats.INCORRECT_OPERATIONS);
                    out.println("ERROR");
                }
            }
        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing client socket: " + e.getMessage());
            }
            changeStats(Stats.CONNECTED_CLIENTS, -1);
            System.out.println("src.Client disconnected.");
        }
    }
    private synchronized void reportStatistics() {
        System.out.println("Reporting statistics...");
        System.out.println("\n=== Global Statistics ===");
        globalStats.forEach((key, value) -> System.out.println(key + ": " + value));

        System.out.println("\n=== Last 10 Seconds Statistics ===");
        lastStats.forEach((key, value) -> System.out.println(key + ": " + value));

        lastStats.replaceAll((key, value) -> 0);
    }

    private int performOperation(String operation, int arg1, int arg2) {
        switch (operation) {
            case "ADD":
                incrementStats(Stats.ADD);
                return arg1 + arg2;
            case "SUB":
                incrementStats(Stats.SUB);
                return arg1 - arg2;
            case "MUL":
                incrementStats(Stats.MUL);
                return arg1 * arg2;
            case "DIV":
                if (arg2 == 0) {
                    throw new ArithmeticException();
                }
                incrementStats(Stats.DIV);
                return arg1 / arg2;
            default:
                throw new IllegalArgumentException();
        }
    }
    private synchronized void changeStats(Stats stats, int value){
        if (stats == Stats.CONNECTED_CLIENTS) {
            globalStats.merge(stats, value, (oldVal, newVal) -> Math.max(0, oldVal + newVal));
            lastStats.merge(stats, value, (oldVal, newVal) -> Math.max(0, oldVal + newVal));
        } else {
            globalStats.merge(stats, value, Integer::sum);
            lastStats.merge(stats, value, Integer::sum);
        }
    }
    private synchronized void incrementStats(Stats stats) {
        changeStats(stats, 1);
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java -jar src.CCS.jar <port>");
            return;
        }
        int port;
        try {
            port = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.out.println("Invalid port number: " + args[0]);
            return;
        }
        CCS server = new CCS(port);
        server.start();
    }
}

