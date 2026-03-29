package org.company.cs455project2;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FundraisingEvent implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String eventName;
    private double targetAmount;
    private double currentAmount;
    private LocalDateTime deadline;
    private LocalDateTime creationTime;
    
    /**
     * Constructor for FundraisingEvent
     * 
     * @param eventName name of the fundraising event
     * @param targetAmount target amount to raise
     * @param deadline deadline for the event (LocalDateTime)
     */
    public FundraisingEvent(String eventName, double targetAmount, LocalDateTime deadline) {
        this.eventName = eventName;
        this.targetAmount = targetAmount;
        this.deadline = deadline;
        this.currentAmount = 0.0;
        this.creationTime = LocalDateTime.now();
    }
    
    // Getters
    public synchronized String getEventName() {
        return eventName;
    }
    
    public synchronized double getTargetAmount() {
        return targetAmount;
    }
    
    public synchronized double getCurrentAmount() {
        return currentAmount;
    }
    
    public synchronized LocalDateTime getDeadline() {
        return deadline;
    }
    
    public synchronized LocalDateTime getCreationTime() {
        return creationTime;
    }
    
    /**
     * Check if the event is currently active.
     * An event is current if the deadline has not passed yet.
     * 
     * @return true if the event is current, false if it's past
     */
    public synchronized boolean isCurrent() {
        return LocalDateTime.now().isBefore(deadline);
    }
    
    /**
     * Get the status of the event (Current or Past).
     * 
     * @return "Current" or "Past"
     */
    public synchronized String getStatus() {
        return isCurrent() ? "Current" : "Past";
    }
    
    /**
     * Process a donation to this event.
     * This method is synchronized to ensure thread-safe updates when multiple clients donate.
     * 
     * @param amount the donation amount
     * @return true if donation was successful, false if invalid
     */
    public synchronized boolean donate(double amount) {
        // Validate donation amount
        if (amount < Constants.MIN_DONATION_AMOUNT || amount > Constants.MAX_DONATION_AMOUNT) {
            return false;
        }
        
        // Allow donations to past events as well (for flexibility)
        this.currentAmount += amount;
        return true;
    }
    
    /**
     * Get remaining amount needed to reach target.
     * 
     * @return remaining amount needed
     */
    public synchronized double getRemainingAmount() {
        double remaining = targetAmount - currentAmount;
        return remaining > 0 ? remaining : 0;
    }
    
    /**
     * Get progress percentage towards target.
     * 
     * @return progress as a percentage (0-100+)
     */
    public synchronized double getProgressPercentage() {
        if (targetAmount == 0) {
            return 0;
        }
        return (currentAmount / targetAmount) * 100;
    }
    
    /**
     * Check if the target amount has been reached.
     * 
     * @return true if current amount >= target amount
     */
    public synchronized boolean isTargetReached() {
        return currentAmount >= targetAmount;
    }
    
    @Override
    public synchronized String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.LOG_DATE_FORMAT);
        return String.format(
            "Event: %s | Target: $%.2f | Current: $%.2f | Deadline: %s | Status: %s | Progress: %.1f%%",
            eventName,
            targetAmount,
            currentAmount,
            deadline.format(formatter),
            getStatus(),
            getProgressPercentage()
        );
    }
}
