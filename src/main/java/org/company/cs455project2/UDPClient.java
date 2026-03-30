package org.company.cs455project2;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * UDPClient handles communication with the UDP fundraising server.
 * Sends requests and receives responses.
 */
public class UDPClient {
    private String serverIP;
    private int serverPort;
    private DatagramSocket socket;
    private DateTimeFormatter dateTimeFormatter;
    
    /**
     * Constructor for UDPClient
     * 
     * @param serverIP the IP address of the server
     * @param serverPort the port of the server
     */
    public UDPClient(String serverIP, int serverPort) {
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.dateTimeFormatter = DateTimeFormatter.ofPattern(Constants.LOG_DATE_FORMAT);
    }
    
    /**
     * Connect to the server (create a DatagramSocket).
     * 
     * @return true if connection successful, false otherwise
     */
    public boolean connect() {
        try {
            socket = new DatagramSocket();
            socket.setSoTimeout(Constants.SOCKET_TIMEOUT_MS);
            logMessage("Connected to server at " + serverIP + ":" + serverPort);
            return true;
        } catch (Exception e) {
            logError("Failed to create socket: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Disconnect from the server (close the socket).
     */
    public void disconnect() {
        if (socket != null && !socket.isClosed()) {
            socket.close();
            logMessage("Disconnected from server");
        }
    }
    
    /**
     * Send a request to the server and get the response.
     * 
     * @param request the Request object to send
     * @return Response object from server, or null if communication failed
     */
    public Response sendRequest(Request request) {
        try {
            if (socket == null || socket.isClosed()) {
                logError("Socket is not connected");
                return null;
            }
            
            // Serialize the request
            byte[] requestData = MessageProtocol.serializeRequest(request);
            
            // Check size
            if (!MessageProtocol.isValidSize(requestData)) {
                logError("Request size exceeds buffer limit");
                return null;
            }
            
            // Send to server
            InetAddress serverAddress = InetAddress.getByName(serverIP);
            DatagramPacket sendPacket = new DatagramPacket(
                requestData,
                requestData.length,
                serverAddress,
                serverPort
            );
            
            logMessage("Sending request: " + request.getRequestType());
            socket.send(sendPacket);
            
            // Receive response
            byte[] receiveBuffer = new byte[Constants.BUFFER_SIZE];
            DatagramPacket receivePacket = new DatagramPacket(
                receiveBuffer,
                receiveBuffer.length
            );
            
            socket.receive(receivePacket);
            
            // Deserialize response
            byte[] responseData = new byte[receivePacket.getLength()];
            System.arraycopy(receivePacket.getData(), 0, responseData, 0, receivePacket.getLength());
            
            Response response = MessageProtocol.deserializeResponse(responseData);
            logMessage("Response received: " + response.getMessage());
            
            return response;
            
        } catch (SocketTimeoutException e) {
            logError("Request timed out. Server may be unavailable.");
            return null;
        } catch (IOException e) {
            logError("Communication error: " + e.getMessage());
            return null;
        } catch (ClassNotFoundException e) {
            logError("Failed to deserialize response: " + e.getMessage());
            return null;
        } catch (Exception e) {
            logError("Unexpected error: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Log a client message with timestamp.
     * Format: [TIMESTAMP] [CLIENT] MESSAGE
     * 
     * @param message the message to log
     */
    private void logMessage(String message) {
        String timestamp = LocalDateTime.now().format(dateTimeFormatter);
        System.out.println(String.format(
            "[%s] [CLIENT] %s",
            timestamp,
            message
        ));
    }
    
    /**
     * Log a client error message with timestamp.
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
}
