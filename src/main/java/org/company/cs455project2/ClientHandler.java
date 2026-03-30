package org.company.cs455project2;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ClientHandler handles a single client connection in a separate thread.
 * Each instance processes one request from one client and sends back a response.
 * Implements Runnable to run in a dedicated thread pool.
 */
public class ClientHandler implements Runnable {
    private DatagramSocket socket;
    private DatagramPacket receivedPacket;
    private String clientIP;
    private int clientPort;
    private RequestProcessor requestProcessor;
    private DateTimeFormatter dateTimeFormatter;
    
    /**
     * Constructor for ClientHandler
     * 
     * @param socket the shared DatagramSocket
     * @param receivedPacket the DatagramPacket containing the client's request
     */
    public ClientHandler(DatagramSocket socket, DatagramPacket receivedPacket) {
        this.socket = socket;
        this.receivedPacket = receivedPacket;
        this.clientIP = receivedPacket.getAddress().getHostAddress();
        this.clientPort = receivedPacket.getPort();
        this.requestProcessor = new RequestProcessor();
        this.dateTimeFormatter = DateTimeFormatter.ofPattern(Constants.LOG_DATE_FORMAT);
    }
    
    /**
     * Run method - entry point for the thread.
     * Receives request from client, processes it, and sends response back.
     */
    @Override
    public void run() {
        try {
            // Log client connection
            logRequest("CLIENT CONNECTED");
            
            // Deserialize the request
            byte[] requestData = new byte[receivedPacket.getLength()];
            System.arraycopy(receivedPacket.getData(), 0, requestData, 0, receivedPacket.getLength());
            
            Request request;
            try {
                request = MessageProtocol.deserializeRequest(requestData);
            } catch (ClassNotFoundException | IOException e) {
                logError("Failed to deserialize request: " + e.getMessage());
                sendErrorResponse("Failed to deserialize request", Constants.BAD_REQUEST);
                return;
            }
            
            // Log request details
            logRequest("REQUEST RECEIVED: " + request.getRequestType().getDescription());
            
            // Process the request
            Response response;
            try {
                response = requestProcessor.processRequest(request);
                logRequest("REQUEST PROCESSED: " + request.getRequestType() + " - Status: " + response.getStatusCode());
            } catch (Exception e) {
                logError("Error processing request: " + e.getMessage());
                response = Response.serverError("Unexpected server error: " + e.getMessage());
            }
            
            // Serialize and send response
            try {
                byte[] responseData = MessageProtocol.serializeResponse(response);
                
                // Check size limits
                if (!MessageProtocol.isValidSize(responseData)) {
                    logError("Response size exceeds buffer limit: " + responseData.length + " bytes");
                    sendErrorResponse("Response too large", Constants.SERVER_ERROR);
                    return;
                }
                
                // Send response back to client
                InetAddress clientAddress = receivedPacket.getAddress();
                DatagramPacket responsePacket = new DatagramPacket(
                    responseData,
                    responseData.length,
                    clientAddress,
                    clientPort
                );
                
                socket.send(responsePacket);
                logRequest("RESPONSE SENT: " + response.getMessage());
                
            } catch (IOException e) {
                logError("Failed to serialize or send response: " + e.getMessage());
            }
            
            // Log client disconnection
            logRequest("CLIENT DISCONNECTED");
            
        } catch (Exception e) {
            logError("Unexpected error in ClientHandler: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Send an error response to the client.
     * Used when request parsing or processing fails before creating a proper Response.
     * 
     * @param message error message
     * @param statusCode HTTP-like status code
     */
    private void sendErrorResponse(String message, int statusCode) {
        try {
            Response errorResponse = new Response(statusCode, false, message);
            byte[] responseData = MessageProtocol.serializeResponse(errorResponse);
            
            InetAddress clientAddress = receivedPacket.getAddress();
            DatagramPacket responsePacket = new DatagramPacket(
                responseData,
                responseData.length,
                clientAddress,
                clientPort
            );
            
            socket.send(responsePacket);
            logRequest("ERROR RESPONSE SENT: " + message);
            
        } catch (IOException e) {
            logError("Failed to send error response: " + e.getMessage());
        }
    }
    
    /**
     * Log a request/response event with client information and timestamp.
     * Format: [TIMESTAMP] [CLIENT IP:PORT] MESSAGE
     * 
     * @param message the message to log
     */
    private void logRequest(String message) {
        String timestamp = LocalDateTime.now().format(dateTimeFormatter);
        System.out.println(String.format(
            "[%s] [CLIENT %s:%d] %s",
            timestamp,
            clientIP,
            clientPort,
            message
        ));
    }
    
    /**
     * Log an error event with client information and timestamp.
     * Format: [TIMESTAMP] [ERROR] [CLIENT IP:PORT] MESSAGE
     * 
     * @param message the error message to log
     */
    private void logError(String message) {
        String timestamp = LocalDateTime.now().format(dateTimeFormatter);
        System.err.println(String.format(
            "[%s] [ERROR] [CLIENT %s:%d] %s",
            timestamp,
            clientIP,
            clientPort,
            message
        ));
    }
}
