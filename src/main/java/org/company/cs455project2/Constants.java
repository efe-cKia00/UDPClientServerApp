package org.company.cs455project2;

public class Constants {
    // Server configuration
    public static final int SERVER_PORT = 9999;
    public static final String SERVER_HOST = "localhost";
    
    // UDP buffer and timeout settings
    public static final int BUFFER_SIZE = 4096;
    public static final int SOCKET_TIMEOUT_MS = 30000; // 30 seconds
    
    // Request/Response size limits
    public static final int MAX_EVENT_NAME_LENGTH = 100;
    public static final int MAX_MESSAGE_LENGTH = 2048;
    
    // Application constants
    public static final double MIN_DONATION_AMOUNT = 0.01;
    public static final double MAX_DONATION_AMOUNT = 1000000.00;
    
    // Logging format
    public static final String LOG_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    
    // Response codes
    public static final int SUCCESS = 200;
    public static final int BAD_REQUEST = 400;
    public static final int SERVER_ERROR = 500;
    
    // Client and server thread naming
    public static final String CLIENT_HANDLER_THREAD_PREFIX = "ClientHandler-";
    
    // Delimiter for serialization (if using custom format)
    public static final String FIELD_DELIMITER = "|";
    public static final String RECORD_DELIMITER = "~";
}
