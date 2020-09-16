package com.cscie97.ledger;

/**
 * LedgerException class supports the Ledger API returning errors
 * LedgerException captures the attempted action and the reason for failure
 *
 * @author Mariam Gogia
 */
public class LedgerException extends Exception{

    // LedgerException properties
    private String action;
    private String reason;

    /**
     * LedgerException constructor
     * @param action - action that caused error
     * @param reason - the reason it failed
     */
    public LedgerException(String action, String reason){
        super(action + ": " + reason);
        this.action = action;
        this.reason = reason;
    }

    // Getters and Setters
    public String getReason() {
        return this.reason;
    }

    public String getAction() {
        return this.action;
    }

    public void setAction(String action) { this.action = action; }

    public void setReason(String reason) { this.reason = reason; }
}
