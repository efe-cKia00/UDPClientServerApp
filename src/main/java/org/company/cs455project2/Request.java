package org.company.cs455project2;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Request implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private RequestType requestType;
    private String clientIP;
    private int clientPort;
    private long timestamp;
    private Map<String, String> parameters;
    
    /**
     * Constructor for Request
     * 
     * @param requestType the type of request
     * @param clientIP the IP address of the client
     * @param clientPort the port of the client
     */
    public Request(RequestType requestType, String clientIP, int clientPort) {
        this.requestType = requestType;
        this.clientIP = clientIP;
        this.clientPort = clientPort;
        this.timestamp = System.currentTimeMillis();
        this.parameters = new HashMap<>();
    }
    
    // Getters
    public RequestType getRequestType() {
        return requestType;
    }
    
    public String getClientIP() {
        return clientIP;
    }
    
    public int getClientPort() {
        return clientPort;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public Map<String, String> getParameters() {
        return parameters;
    }
    
    /**
     * Add a parameter to the request
     * 
     * @param key parameter key
     * @param value parameter value
     */
    public void addParameter(String key, String value) {
        parameters.put(key, value);
    }
    
    /**
     * Get a parameter value
     * 
     * @param key parameter key
     * @return parameter value or null if not found
     */
    public String getParameter(String key) {
        return parameters.get(key);
    }
    
    @Override
    public String toString() {
        return String.format(
            "Request{type=%s, client=%s:%d, timestamp=%d, params=%s}",
            requestType,
            clientIP,
            clientPort,
            timestamp,
            parameters
        );
    }
}
