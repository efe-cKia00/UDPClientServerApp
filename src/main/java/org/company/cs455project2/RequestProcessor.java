package org.company.cs455project2;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * RequestProcessor handles incoming client requests and routes them to appropriate handlers.
 * Validates request parameters and returns appropriate responses.
 */
public class RequestProcessor {
    private FundManager fundManager;
    private DateTimeFormatter dateTimeFormatter;
    
    /**
     * Constructor for RequestProcessor
     */
    public RequestProcessor() {
        this.fundManager = FundManager.getInstance();
        this.dateTimeFormatter = DateTimeFormatter.ofPattern(Constants.LOG_DATE_FORMAT);
    }
    
    /**
     * Process a client request and return a response.
     * Routes to appropriate handler based on request type.
     * 
     * @param request the incoming Request object
     * @return Response object with status and data
     */
    public Response processRequest(Request request) {
        if (request == null || request.getRequestType() == null) {
            return Response.badRequest("Invalid request: request or request type is null");
        }
        
        return switch (request.getRequestType()) {
            case CREATE_EVENT -> handleCreateEvent(request);
            case LIST_CURRENT -> handleListCurrentEvents(request);
            case LIST_PAST -> handleListPastEvents(request);
            case DONATE -> handleDonate(request);
            default -> Response.badRequest("Unknown request type: " + request.getRequestType());
        };
    }
    
    /**
     * Handle CREATE_EVENT request.
     * Expected parameters: eventName, targetAmount, deadline
     * 
     * @param request the request
     * @return Response with created event details or error message
     */
    private Response handleCreateEvent(Request request) {
        try {
            // Validate parameters
            String eventName = request.getParameter("eventName");
            String targetAmountStr = request.getParameter("targetAmount");
            String deadlineStr = request.getParameter("deadline");
            
            // Ensure valid event name, target amount and deadline
            if (eventName == null || eventName.trim().isEmpty()) {
                return Response.badRequest("Invalid request: eventName is required");
            }
            
            if (targetAmountStr == null || targetAmountStr.trim().isEmpty()) {
                return Response.badRequest("Invalid request: targetAmount is required");
            }
            
            if (deadlineStr == null || deadlineStr.trim().isEmpty()) {
                return Response.badRequest("Invalid request: deadline is required");
            }
            
            // Parse and validate targetAmount
            double targetAmount;
            try {
                targetAmount = Double.parseDouble(targetAmountStr);
                if (targetAmount <= 0) {
                    return Response.badRequest("Invalid targetAmount: must be greater than 0");
                }
            } catch (NumberFormatException e) {
                return Response.badRequest("Invalid targetAmount: must be a valid number");
            }
            
            // Parse and validate deadline
            LocalDateTime deadline;
            try {
                deadline = LocalDateTime.parse(deadlineStr, dateTimeFormatter);
            } catch (DateTimeParseException e) {
                return Response.badRequest("Invalid deadline format. Expected format: yyyy-MM-dd HH:mm:ss.SSS");
            }
            
            // Create event
            FundraisingEvent event = fundManager.createEvent(eventName, targetAmount, deadline);
            
            // Failed response
            if (event == null) {
                return Response.badRequest("Failed to create event. Check event name length and deadline validity.");
            }
            
            // Success response
            Response response = Response.success("Event created successfully");
            response.addData("eventName", event.getEventName());
            response.addData("targetAmount", String.format("%.2f", event.getTargetAmount()));
            response.addData("deadline", event.getDeadline().format(dateTimeFormatter));
            response.addData("eventIndex", String.valueOf(fundManager.getEventCount() - 1));
            
            return response;
            
        } catch (Exception e) {
            return Response.serverError("Server error while creating event: " + e.getMessage());
        }
    }
    
    /**
     * Handle LIST_CURRENT request.
     * Returns all current (active) fundraising events.
     * 
     * @param request the request
     * @return Response with list of current events
     */
    private Response handleListCurrentEvents(Request request) {
        try {
            List<FundraisingEvent> currentEvents = fundManager.listCurrentEvents();
            
            if (currentEvents.isEmpty()) {
                Response response = Response.success("No current events available");
                response.addData("eventCount", "0");
                return response;
            }
            
            Response response = Response.success("Current events retrieved successfully");
            response.addData("eventCount", String.valueOf(currentEvents.size()));
            
            // Add each event to response
            for (int i = 0; i < currentEvents.size(); i++) {
                FundraisingEvent event = currentEvents.get(i);
                String eventKey = "event_" + i;
                response.addData(eventKey + "_name", event.getEventName());
                response.addData(eventKey + "_target", String.format("%.2f", event.getTargetAmount()));
                response.addData(eventKey + "_current", String.format("%.2f", event.getCurrentAmount()));
                response.addData(eventKey + "_remaining", String.format("%.2f", event.getRemainingAmount()));
                response.addData(eventKey + "_progress", String.format("%.1f", event.getProgressPercentage()));
                response.addData(eventKey + "_deadline", event.getDeadline().format(dateTimeFormatter));
            }
            
            return response;
            
        } catch (Exception e) {
            return Response.serverError("Server error while listing current events: " + e.getMessage());
        }
    }
    
    /**
     * Handle LIST_PAST request.
     * Returns all past (expired) fundraising events.
     * 
     * @param request the request
     * @return Response with list of past events
     */
    private Response handleListPastEvents(Request request) {
        try {
            List<FundraisingEvent> pastEvents = fundManager.listPastEvents();
            
            if (pastEvents.isEmpty()) {
                Response response = Response.success("No past events available");
                response.addData("eventCount", "0");
                return response;
            }
            
            Response response = Response.success("Past events retrieved successfully");
            response.addData("eventCount", String.valueOf(pastEvents.size()));
            
            // Add each event to response
            for (int i = 0; i < pastEvents.size(); i++) {
                FundraisingEvent event = pastEvents.get(i);
                String eventKey = "event_" + i;
                response.addData(eventKey + "_name", event.getEventName());
                response.addData(eventKey + "_target", String.format("%.2f", event.getTargetAmount()));
                response.addData(eventKey + "_current", String.format("%.2f", event.getCurrentAmount()));
                response.addData(eventKey + "_remaining", String.format("%.2f", event.getRemainingAmount()));
                response.addData(eventKey + "_progress", String.format("%.1f", event.getProgressPercentage()));
                response.addData(eventKey + "_deadline", event.getDeadline().format(dateTimeFormatter));
                response.addData(eventKey + "_targetReached", String.valueOf(event.isTargetReached()));
            }
            
            return response;
            
        } catch (Exception e) {
            return Response.serverError("Server error while listing past events: " + e.getMessage());
        }
    }
    
    /**
     * Handle DONATE request.
     * Expected parameters: eventIndex, amount, isCurrentEvent
     * 
     * @param request the request
     * @return Response with donation confirmation or error message
     */
    private Response handleDonate(Request request) {
        try {
            // Validate parameters
            String eventIndexStr = request.getParameter("eventIndex");
            String amountStr = request.getParameter("amount");
            String isCurrentEventStr = request.getParameter("isCurrentEvent");
            
            if (eventIndexStr == null || eventIndexStr.trim().isEmpty()) {
                return Response.badRequest("Invalid request: eventIndex is required");
            }
            
            if (amountStr == null || amountStr.trim().isEmpty()) {
                return Response.badRequest("Invalid request: amount is required");
            }
            
            if (isCurrentEventStr == null || isCurrentEventStr.trim().isEmpty()) {
                return Response.badRequest("Invalid request: isCurrentEvent is required");
            }
            
            // Parse and validate eventIndex
            int eventIndex;
            try {
                eventIndex = Integer.parseInt(eventIndexStr);
                if (eventIndex < 0) {
                    return Response.badRequest("Invalid eventIndex: must be non-negative");
                }
            } catch (NumberFormatException e) {
                return Response.badRequest("Invalid eventIndex: must be a valid integer");
            }
            
            // Parse and validate amount
            double amount;
            try {
                amount = Double.parseDouble(amountStr);
                if (amount < Constants.MIN_DONATION_AMOUNT || amount > Constants.MAX_DONATION_AMOUNT) {
                    return Response.badRequest(
                        String.format("Invalid amount: must be between $%.2f and $%.2f",
                            Constants.MIN_DONATION_AMOUNT,
                            Constants.MAX_DONATION_AMOUNT)
                    );
                }
            } catch (NumberFormatException e) {
                return Response.badRequest("Invalid amount: must be a valid number");
            }
            
            // Parse isCurrentEvent boolean
            boolean isCurrent = Boolean.parseBoolean(isCurrentEventStr);
            
            // Get the event
            FundraisingEvent event = fundManager.getEventByIndexInCategory(eventIndex, isCurrent);
            
            if (event == null) {
                String category = isCurrent ? "current" : "past";
                return Response.badRequest(
                    "Invalid event index for " + category + " events. Event not found."
                );
            }
            
            // Process donation
            boolean donationSuccess = fundManager.donateToEventInCategory(eventIndex, amount, isCurrent);
            
            if (!donationSuccess) {
                return Response.badRequest("Donation failed. Invalid amount or event.");
            }
            
            // Build success response
            Response response = Response.success("Donation processed successfully");
            response.addData("eventName", event.getEventName());
            response.addData("donationAmount", String.format("%.2f", amount));
            response.addData("newTotal", String.format("%.2f", event.getCurrentAmount()));
            response.addData("remaining", String.format("%.2f", event.getRemainingAmount()));
            response.addData("targetReached", String.valueOf(event.isTargetReached()));
            response.addData("progress", String.format("%.1f", event.getProgressPercentage()));
            
            return response;
            
        } catch (Exception e) {
            return Response.serverError("Server error while processing donation: " + e.getMessage());
        }
    }
}
