package com.cscie97.ledger;

/**
 * Transaction class represents a transaction in the Ledger System
 * it contains transaction id, amount, fee, a note,
 * and references to payer and receiver accounts
 *
 * @author Mariam Gogia
 */
public class Transaction {
    // Transaction class properties and associations
    private String transactionID;
    private int amount;
    private int fee;
    private String note;
    private Account payer;
    private Account receiver;


    /**
     * Transaction constructor
     * @param transactionID - unique ID
     * @param amount - the amount to be transferred
     * @param fee - the fee
     * @param note - transaction note
     * @param payer - a reference to the payer account
     * @param receiver - a reference to the receiver account
     */
    public Transaction(String transactionID, int amount, int fee, String note, Account payer, Account receiver) {
        this.transactionID = transactionID;
        this.amount = amount;
        this.fee = fee;
        this.note = note;
        this.payer = payer;
        this.receiver =receiver;
    }

    // a helper method to print transaction details
    public String toString(){
        return "Transaction ID: " + transactionID + " Amount: " + amount + " Fee: " + fee
                + " Note: " + note + " Payer ID: " + payer.getAddress() + " Receiver ID: " + receiver.getAddress();
    }

    // Getters and Setters
    public Account getPayer() {
        return payer;
    }

    public void setPayer(Account payer) {
        this.payer = payer;
    }

    public Account getReceiver() {
        return receiver;
    }

    public void setReceiver(Account receiver) {
        this.receiver = receiver;
    }

    public String getTransactionID() {
        return this.transactionID;
    }

    public void setTransactionID(String transactionID) {
        this.transactionID = transactionID;
    }

    public int getAmount() {
        return this.amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getNote() {
        return this.note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public int getFee() {
        return this.fee;
    }

    public void setFee(int fee) {
        this.fee = fee;
    }
}
