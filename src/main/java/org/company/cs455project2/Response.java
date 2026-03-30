package org.company.cs455project2;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Response implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int statusCode;
    private boolean success;
    private String message;
    private Map<String, String> data;
    private long timestamp;
    
    /**
     * Constructor for Response
     * 
     * @param statusCode HTTP-like status code (200, 400, 500, etc.)
     * @param success whether the request was successful
     * @param message descriptive message
     */
    public Response(int statusCode, boolean success, String message) {
        this.statusCode = statusCode;
        this.success = success;
        this.message = message;
        this.data = new HashMap<>();
        this.timestamp = System.currentTimeMillis();
    }
    
    // Getters
    public int getStatusCode() {
        return statusCode;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public Map<String, String> getData() {
        return data;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * Add data to the response
     * 
     * @param key data key
     * @param value data value
     */
    public void addData(String key, String value) {
        data.put(key, value);
    }
    
    /**
     * Get data value
     * 
     * @param key data key
     * @return data value or null if not found
     */
    public String getData(String key) {
        return data.get(key);
    }
    
    /**
     * Factory method for successful response
     * 
     * @param message success message
     * @return Response object with status 200
     */
    public static Response success(String message) {
        return new Response(Constants.SUCCESS, true, message);
    }
    
    /**
     * Factory method for bad request response
     * 
     * @param message error message
     * @return Response object with status 400
     */
    public static Response badRequest(String message) {
        return new Response(Constants.BAD_REQUEST, false, message);
    }
    
    /**
     * Factory method for server error response
     * 
     * @param message error message
     * @return Response object with status 500
     */
    public static Response serverError(String message) {
        return new Response(Constants.SERVER_ERROR, false, message);
    }
    
    @Override
    public String toString() {
        return String.format(
            "Response{status=%d, success=%b, message='%s', data=%s, timestamp=%d}",
            statusCode,
            success,
            message,
            data,
            timestamp
        );
    }
}
