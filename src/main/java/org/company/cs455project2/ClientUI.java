package org.company.cs455project2;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Scanner;

/**
 * ClientUI is the user interface for the fundraising donation client.
 * Displays menus and handles user input.
 */
public class ClientUI {
    private UDPClient udpClient;
    private Scanner scanner;
    private DateTimeFormatter dateTimeFormatter;
    private boolean running;
    
    /**
     * Constructor for ClientUI
     * 
     * @param serverIP the IP address of the server
     * @param serverPort the port of the server
     */
    public ClientUI(String serverIP, int serverPort) {
        this.udpClient = new UDPClient(serverIP, serverPort);
        this.scanner = new Scanner(System.in);
        this.dateTimeFormatter = DateTimeFormatter.ofPattern(Constants.LOG_DATE_FORMAT);
        this.running = false;
    }
    
    /**
     * Start the client UI - main interaction loop.
     */
    public void start() {
        // Connect to server
        if (!udpClient.connect()) {
            System.out.println("\n*** Failed to connect to server ***\n");
            return;
        }
        
        running = true;
        System.out.println("\n========================================");
        System.out.println("Fundraising Donation Client");
        System.out.println("Server: " + Constants.SERVER_HOST + ":" + Constants.SERVER_PORT);
        System.out.println("========================================\n");
        
        // Main menu loop
        while (running) {
            displayMainMenu();
            int choice = getIntInput("Enter your choice: ", -1);
            
            switch (choice) {
                case 1:
                    handleCreateEvent();
                    break;
                case 2:
                    handleListCurrentEvents();
                    break;
                case 3:
                    handleListPastEvents();
                    break;
                case 4:
                    handleDonate();
                    break;
                case 5:
                    running = false;
                    System.out.println("\nExiting client...");
                    break;
                default:
                    System.out.println("*** Invalid choice. Please try again. ***\n");
            }
        }
        
        // Disconnect from server
        udpClient.disconnect();
        scanner.close();
    }
    
    /**
     * Display the main menu.
     */
    private void displayMainMenu() {
        System.out.println("\n--- Main Menu ---");
        System.out.println("1. Create a new fundraising event");
        System.out.println("2. List current events");
        System.out.println("3. List past events");
        System.out.println("4. Donate to an event");
        System.out.println("5. Exit");
    }
    
    /**
     * Handle create event request.
     */
    private void handleCreateEvent() {
        System.out.println("\n--- Create New Event ---");
        
        String eventName = getStringInput("Enter event name: ");
        if (eventName == null || eventName.trim().isEmpty()) {
            System.out.println("*** Event name cannot be empty. ***\n");
            return;
        }
        
        if (eventName.length() > Constants.MAX_EVENT_NAME_LENGTH) {
            System.out.println("*** Event name too long (max " + Constants.MAX_EVENT_NAME_LENGTH + " characters). ***\n");
            return;
        }
        
        double targetAmount = getDoubleInput("Enter target amount ($): ", -1);
        if (targetAmount <= 0) {
            System.out.println("*** Target amount must be greater than 0. ***\n");
            return;
        }
        
        System.out.println("\nEnter deadline (format: yyyy-MM-dd HH:mm:ss.SSS)");
        System.out.println("Example: 2026-12-31 23:59:59.999");
        String deadlineStr = getStringInput("Deadline: ");
        
        // Validate deadline format
        LocalDateTime deadline = null;
        try {
            deadline = LocalDateTime.parse(deadlineStr, dateTimeFormatter);
            if (deadline.isBefore(LocalDateTime.now())) {
                System.out.println("*** Deadline cannot be in the past. ***\n");
                return;
            }
        } catch (DateTimeParseException e) {
            System.out.println("*** Invalid deadline format. ***\n");
            return;
        }
        
        // Create request
        Request request = new Request(RequestType.CREATE_EVENT, "localhost", 0);
        request.addParameter("eventName", eventName);
        request.addParameter("targetAmount", String.valueOf(targetAmount));
        request.addParameter("deadline", deadlineStr);
        
        // Send request
        Response response = udpClient.sendRequest(request);
        if (response != null) {
            handleResponse(response);
        }
    }
    
    /**
     * Handle list current events request.
     */
    private void handleListCurrentEvents() {
        System.out.println("\n--- Current Events ---");
        
        Request request = new Request(RequestType.LIST_CURRENT, "localhost", 0);
        Response response = udpClient.sendRequest(request);
        
        if (response != null) {
            handleResponse(response);
            displayEventsList(response, true);
        }
    }
    
    /**
     * Handle list past events request.
     */
    private void handleListPastEvents() {
        System.out.println("\n--- Past Events ---");
        
        Request request = new Request(RequestType.LIST_PAST, "localhost", 0);
        Response response = udpClient.sendRequest(request);
        
        if (response != null) {
            handleResponse(response);
            displayEventsList(response, false);
        }
    }
    
    /**
     * Handle donate to event request.
     */
    private void handleDonate() {
        System.out.println("\n--- Donate to Event ---");
        
        // First, ask user to choose which category of events
        System.out.println("\nDonate to which type of event?");
        System.out.println("1. Current event");
        System.out.println("2. Past event");
        int typeChoice = getIntInput("Enter your choice: ", -1);
        
        boolean isCurrent = true;
        if (typeChoice == 1) {
            isCurrent = true;
        } else if (typeChoice == 2) {
            isCurrent = false;
        } else {
            System.out.println("*** Invalid choice. ***\n");
            return;
        }
        
        // List events in the chosen category
        Request listRequest = new Request(
            isCurrent ? RequestType.LIST_CURRENT : RequestType.LIST_PAST,
            "localhost",
            0
        );
        Response listResponse = udpClient.sendRequest(listRequest);
        
        if (listResponse == null || !listResponse.isSuccess()) {
            System.out.println("\n*** Failed to retrieve events. ***\n");
            return;
        }
        
        // Display events
        displayEventsList(listResponse, isCurrent);
        
        // Get event index
        int eventIndex = getIntInput("Enter event index: ", -1);
        if (eventIndex < 0) {
            System.out.println("*** Invalid event index. ***\n");
            return;
        }
        
        // Get donation amount
        double amount = getDoubleInput("Enter donation amount ($): ", -1);
        if (amount < Constants.MIN_DONATION_AMOUNT || amount > Constants.MAX_DONATION_AMOUNT) {
            System.out.println(String.format(
                "*** Donation must be between $%.2f and $%.2f. ***\n",
                Constants.MIN_DONATION_AMOUNT,
                Constants.MAX_DONATION_AMOUNT
            ));
            return;
        }
        
        // Create donation request
        Request donateRequest = new Request(RequestType.DONATE, "localhost", 0);
        donateRequest.addParameter("eventIndex", String.valueOf(eventIndex));
        donateRequest.addParameter("amount", String.valueOf(amount));
        donateRequest.addParameter("isCurrentEvent", String.valueOf(isCurrent));
        
        // Send donation request
        Response donateResponse = udpClient.sendRequest(donateRequest);
        if (donateResponse != null) {
            handleResponse(donateResponse);
        }
    }
    
    /**
     * Display a list of events from the response.
     * 
     * @param response the Response object containing events
     * @param isCurrent true if displaying current events, false for past
     */
    private void displayEventsList(Response response, boolean isCurrent) {
        Map<String, String> data = response.getData();
        String eventCountStr = data.get("eventCount");
        
        if (eventCountStr == null) {
            return;
        }
        
        int eventCount = Integer.parseInt(eventCountStr);
        
        if (eventCount == 0) {
            System.out.println("No events available.\n");
            return;
        }
        
        System.out.println();
        for (int i = 0; i < eventCount; i++) {
            String eventKey = "event_" + i;
            String name = data.get(eventKey + "_name");
            String target = data.get(eventKey + "_target");
            String current = data.get(eventKey + "_current");
            String remaining = data.get(eventKey + "_remaining");
            String progress = data.get(eventKey + "_progress");
            String deadline = data.get(eventKey + "_deadline");
            String targetReached = data.get(eventKey + "_targetReached");
            
            System.out.println(String.format(
                "[%d] %s | Target: $%s | Current: $%s | Progress: %s%% | Deadline: %s",
                i,
                name,
                target,
                current,
                progress,
                deadline
            ));
            
            if (!isCurrent && targetReached != null) {
                System.out.println("    Target reached: " + targetReached);
            }
        }
        System.out.println();
    }
    
    /**
     * Handle a response and display the result.
     * 
     * @param response the Response object
     */
    private void handleResponse(Response response) {
        if (response.isSuccess()) {
            System.out.println("\n*** Success: " + response.getMessage() + " ***");
        } else {
            System.out.println("\n*** Error: " + response.getMessage() + " ***");
        }
    }
    
    /**
     * Get string input from user.
     * 
     * @param prompt the prompt to display
     * @return the user input
     */
    private String getStringInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }
    
    /**
     * Get integer input from user with validation.
     * 
     * @param prompt the prompt to display
     * @param defaultValue the default value if input is invalid
     * @return the user input or default value
     */
    private int getIntInput(String prompt, int defaultValue) {
        System.out.print(prompt);
        try {
            String input = scanner.nextLine().trim();
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * Get double input from user with validation.
     * 
     * @param prompt the prompt to display
     * @param defaultValue the default value if input is invalid
     * @return the user input or default value
     */
    private double getDoubleInput(String prompt, double defaultValue) {
        System.out.print(prompt);
        try {
            String input = scanner.nextLine().trim();
            return Double.parseDouble(input);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * Main method to start the client.
     * 
     * @param args command line arguments (serverIP and serverPort optional)
     */
    public static void main(String[] args) {
        String serverIP = Constants.SERVER_HOST;
        int serverPort = Constants.SERVER_PORT;
        
        // Parse command line arguments if provided
        if (args.length >= 1) {
            serverIP = args[0];
        }
        if (args.length >= 2) {
            try {
                serverPort = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid port number. Using default: " + Constants.SERVER_PORT);
            }
        }
        
        ClientUI ui = new ClientUI(serverIP, serverPort);
        ui.start();
    }
}
