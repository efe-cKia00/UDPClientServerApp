package org.company.cs455project2;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * UDPServer is the main server class that listens for incoming UDP client requests.
 * For each incoming request, a new ClientHandler thread is spawned to handle the client.
 * Uses a thread pool for efficient concurrent handling of multiple clients.
 */
public class UDPServer {
    private DatagramSocket socket;
    private boolean running;
    private ExecutorService threadPool;
    private DateTimeFormatter dateTimeFormatter;
    private static final int THREAD_POOL_SIZE = 10;
    
    /**
     * Constructor for UDPServer
     */
    public UDPServer() {
        this.running = false;
        this.threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        this.dateTimeFormatter = DateTimeFormatter.ofPattern(Constants.LOG_DATE_FORMAT);
    }
    
    /**
     * Start the server.
     * Creates a DatagramSocket and listens for incoming requests.
     * Each request is handled in a separate thread from the thread pool.
     */
    public void start() {
        try {
            // Create the DatagramSocket
            socket = new DatagramSocket(Constants.SERVER_PORT);
            running = true;
            
            logMessage("========================================");
            logMessage("UDP Fundraising Server Started");
            logMessage("Server Port: " + Constants.SERVER_PORT);
            logMessage("Thread Pool Size: " + THREAD_POOL_SIZE);
            logMessage("Max Buffer Size: " + Constants.BUFFER_SIZE + " bytes");
            logMessage("========================================");
            logMessage("Waiting for client requests...");
            logMessage("");
            
            // Main server loop - listen for incoming requests
            while (running) {
                // Create a buffer to receive data
                byte[] receiveBuffer = new byte[Constants.BUFFER_SIZE];
                DatagramPacket receivePacket = new DatagramPacket(
                    receiveBuffer,
                    receiveBuffer.length
                );
                
                // Receive a request from a client
                socket.receive(receivePacket);
                
                // Spawn a new ClientHandler thread to handle this request
                ClientHandler handler = new ClientHandler(socket, receivePacket);
                threadPool.execute(handler);
            }
            
        } catch (SocketException e) {
            if (running) {
                logError("SocketException: " + e.getMessage());
            }
        } catch (Exception e) {
            logError("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            shutdown();
        }
    }
    
    /**
     * Stop the server gracefully.
     * Closes the socket and shuts down the thread pool.
     */
    public void shutdown() {
        running = false;
        
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                logMessage("Socket closed.");
            }
        } catch (Exception e) {
            logError("Error closing socket: " + e.getMessage());
        }
        
        try {
            logMessage("Shutting down thread pool...");
            threadPool.shutdown();
            
            // Wait for threads to finish (with timeout)
            if (!threadPool.awaitTermination(10, TimeUnit.SECONDS)) {
                logMessage("Thread pool did not terminate in time. Force shutting down...");
                threadPool.shutdownNow();
            }
            logMessage("Thread pool shut down successfully.");
        } catch (InterruptedException e) {
            logError("Interrupted while waiting for thread pool to shutdown: " + e.getMessage());
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        logMessage("========================================");
        logMessage("UDP Fundraising Server Stopped");
        logMessage("========================================");
    }
    
    /**
     * Log a server message with timestamp.
     * Format: [TIMESTAMP] [SERVER] MESSAGE
     * 
     * @param message the message to log
     */
    private void logMessage(String message) {
        String timestamp = LocalDateTime.now().format(dateTimeFormatter);
        System.out.println(String.format(
            "[%s] [SERVER] %s",
            timestamp,
            message
        ));
    }
    
    /**
     * Log a server error message with timestamp.
     * Format: [TIMESTAMP] [ERROR] MESSAGE
     * 
     * @param message the error message to log
     */
    private void logError(String message) {
        String timestamp = LocalDateTime.now().format(dateTimeFormatter);
        System.err.println(String.format(
            "[%s] [ERROR] %s",
            timestamp,
            message
        ));
    }
    
    /**
     * Main method to start the server.
     * 
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        UDPServer server = new UDPServer();
        server.start();
    }
}
