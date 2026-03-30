package org.company.cs455project2;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * FundManager manages all fundraising events with thread-safe operations.
 * Uses singleton pattern to ensure a single instance across the server.
 * All methods are synchronized to handle concurrent access from multiple client threads.
 */
public class FundManager {
    private static FundManager instance;
    private List<FundraisingEvent> events;
    
    /**
     * Private constructor for singleton pattern
     */
    private FundManager() {
        this.events = Collections.synchronizedList(new ArrayList<>());
    }
    
    /**
     * Get the singleton instance of FundManager
     * 
     * @return the FundManager instance
     */
    public static synchronized FundManager getInstance() {
        if (instance == null) {
            instance = new FundManager();
        }
        return instance;
    }
    
    /**
     * Create a new fundraising event.
     * Thread-safe method for handling concurrent event creation requests.
     * 
     * @param eventName name of the event
     * @param targetAmount target fundraising amount
     * @param deadline deadline for the event
     * @return the created FundraisingEvent, or null if invalid input
     */
    public synchronized FundraisingEvent createEvent(String eventName, double targetAmount, LocalDateTime deadline) {
        // Validate input
        if (eventName == null || eventName.trim().isEmpty()) {
            return null;
        }
        
        if (eventName.length() > Constants.MAX_EVENT_NAME_LENGTH) {
            return null;
        }
        
        if (targetAmount <= 0) {
            return null;
        }
        
        if (deadline == null || deadline.isBefore(LocalDateTime.now())) {
            return null;
        }
        
        // Create and add the event
        FundraisingEvent event = new FundraisingEvent(eventName, targetAmount, deadline);
        events.add(event);
        
        return event;
    }
    
    /**
     * Get all current (active) fundraising events, sorted by deadline.
     * 
     * @return list of current events sorted by deadline (earliest first)
     */
    public synchronized List<FundraisingEvent> listCurrentEvents() {
        return events.stream()
                .filter(FundraisingEvent::isCurrent)
                .sorted(Comparator.comparing(FundraisingEvent::getDeadline))
                .collect(Collectors.toList());
    }
    
    /**
     * Get all past (expired) fundraising events, sorted by deadline.
     * 
     * @return list of past events sorted by deadline (most recent first)
     */
    public synchronized List<FundraisingEvent> listPastEvents() {
        return events.stream()
                .filter(event -> !event.isCurrent())
                .sorted(Comparator.comparing(FundraisingEvent::getDeadline).reversed())
                .collect(Collectors.toList());
    }
    
    /**
     * Get an event by its index in the ALL events list.
     * Note: This retrieves from the complete event list (current + past).
     * 
     * @param index the index of the event
     * @return the FundraisingEvent, or null if index is invalid
     */
    public synchronized FundraisingEvent getEventByIndex(int index) {
        if (index < 0 || index >= events.size()) {
            return null;
        }
        return events.get(index);
    }
    
    /**
     * Get an event by its index in a specific category (current or past).
     * 
     * @param index the index within the category
     * @param isCurrent true to get from current events, false for past events
     * @return the FundraisingEvent, or null if index is invalid
     */
    public synchronized FundraisingEvent getEventByIndexInCategory(int index, boolean isCurrent) {
        List<FundraisingEvent> categoryList = isCurrent ? listCurrentEvents() : listPastEvents();
        
        if (index < 0 || index >= categoryList.size()) {
            return null;
        }
        return categoryList.get(index);
    }
    
    /**
     * Process a donation to a specific event by its index.
     * This method is synchronized and works in conjunction with FundraisingEvent's synchronized donate method
     * to ensure data consistency when multiple clients donate to the same event.
     * 
     * @param eventIndex the index of the event
     * @param amount the donation amount
     * @return true if donation was successful, false if failed (invalid index or amount)
     */
    public synchronized boolean donateToEvent(int eventIndex, double amount) {
        FundraisingEvent event = getEventByIndex(eventIndex);
        
        if (event == null) {
            return false;
        }
        
        // The event's donate method is also synchronized for double-checked locking
        return event.donate(amount);
    }
    
    /**
     * Process a donation to an event by its index in a specific category.
     * 
     * @param eventIndex the index within the category
     * @param amount the donation amount
     * @param isCurrent true to donate to current events, false for past events
     * @return true if donation was successful, false if failed
     */
    public synchronized boolean donateToEventInCategory(int eventIndex, double amount, boolean isCurrent) {
        FundraisingEvent event = getEventByIndexInCategory(eventIndex, isCurrent);
        
        if (event == null) {
            return false;
        }
        
        return event.donate(amount);
    }
    
    /**
     * Get all events (current and past).
     * 
     * @return list of all events
     */
    public synchronized List<FundraisingEvent> getAllEvents() {
        return new ArrayList<>(events);
    }
    
    /**
     * Get the total number of events.
     * 
     * @return number of events
     */
    public synchronized int getEventCount() {
        return events.size();
    }
    
    /**
     * Get the number of current events.
     * 
     * @return count of current events
     */
    public synchronized int getCurrentEventCount() {
        return (int) events.stream().filter(FundraisingEvent::isCurrent).count();
    }
    
    /**
     * Get the number of past events.
     * 
     * @return count of past events
     */
    public synchronized int getPastEventCount() {
        return (int) events.stream().filter(event -> !event.isCurrent()).count();
    }
    
    /**
     * Clear all events (useful for testing or server reset).
     */
    public synchronized void clearAllEvents() {
        events.clear();
    }
}
