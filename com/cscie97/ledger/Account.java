package com.cscie97.ledger;

/**
 * The Account class represents individual accounts within the Ledger Service
 * It provides the address and the balance for the Account
 *
 * @author Mariam Gogia
 */
public class Account {
    // Account class properties
    private  String address;
    private  int balance;

    /** Account constructor
     * @param address - unique identifier
     * @param balance - the balance of the account
     */
    public Account(String address, int balance) {
        this.address = address;
        this.balance = balance;
    }

    // a helper method - used in making a deep copy of the account balance map
    public Account(Account account) {
        this.address = account.address;
        this.balance = account.balance;
    }

    // Getters and Setters
    public  String getAddress() { return address; }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }
}
