package com.cscie97.ledger;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Block class supports the blocks in the Ledger Service
 * it accumulates transactions, ensures immutability of blocks by computing hashes
 * maintain the list of transactions and the accountBalance map
 *
 * @author Mariam Gogia
 */
public class Block {

    // block class properties & associations
    private int blockNumber;
    private String previousHash;
    private String hash;
    private Block previousBlock;
    public final List<Transaction> transactionList = new ArrayList<>();
    private Map<String, Account> accountBalanceMap = new HashMap<String, Account>();

    /** Block constructor
     * @param blockNumber - block number
     * @param previousHash - the hash of the previous block
     */
    public Block(int blockNumber, String previousHash) {
        this.blockNumber = blockNumber;
        this.previousHash = previousHash;
    }

    /** a method to add transactions in transactionList
     * @param transaction - transaction
     * @return - true if the list is full, false otherwise
     */
    public boolean addTransaction(Transaction transaction) {
        transactionList.add(transaction);

        // if the list contains 10 transactions - return true
        if(transactionList.size() == 10) {
            return true;
        }
        else {
            return false;
        }
    }

    /** Getter that returns hash of the block,
     * the method calls computeHash to compute the hash
     * @param seed - ledger seed
     * @return - hash
     * @throws NoSuchAlgorithmException throws NoSuchAlgorithmException
     */
    public String getHash(String seed) throws NoSuchAlgorithmException {
        // implementing the Merkel Tree using ArrayList
        ArrayList<String> merkelTree = new ArrayList<>();

        // hash each transaction and add to merkelTree
        for(Transaction transaction : transactionList){
            String transactionToString = transaction.toString();
            merkelTree.add(computeHash(seed, transactionToString));
        }

        int initialIndex = 0;
        // processing the list of 10 elements (transaction hashes), compute hashes for for pair of transactions -
        // pairing 10 elements gives 5 new elements (hashes of pairs of transactions)
        // add those 5 elements to the list and now process the 'new' list of 5 elements - repeat until there are no more new elements to process.
        // A small piece of code is taken from @credit: https://dzone.com/articles/blockchain-implementation-with-java-code
        for (int currentListSize = transactionList.size(); currentListSize > 1; currentListSize = (currentListSize + 1) / 2) {

            for (int leftIndex = 0; leftIndex < currentListSize; leftIndex += 2) {

                // right index
                int rightIndex = Math.min(leftIndex + 1, currentListSize - 1);

               // adds the H(H(T9) + H(T10) at the end
                if (leftIndex == rightIndex) {
                    merkelTree.add(merkelTree.get(initialIndex+leftIndex));
                }
                else {
                    // getting strings from indices
                    String leftStr = merkelTree.get(initialIndex + leftIndex);
                    String rightStr = merkelTree.get(initialIndex + rightIndex);

                    // computing and adding a hash of concatenated string
                    merkelTree.add(computeHash(seed,(leftStr + rightStr)));
                }
            }
            initialIndex += currentListSize;
        }
        // the last element is the 'root' of the merkelTree
        String root = merkelTree.get(merkelTree.size()-1);

        // converting accountBalanceMap to a string and computing the hash of root + map + previousHash
        Object[] accountBalanceMapToArray = accountBalanceMap.entrySet().toArray();
        hash = computeHash(seed, (root + this.previousHash + (Arrays.toString(accountBalanceMapToArray))));
        return hash;
    }

    // helper method computing hash for getHash function
    public String computeHash(String seed, String transactionToString) throws NoSuchAlgorithmException {

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(seed.getBytes(StandardCharsets.UTF_8));
        byte[] hashArray = digest.digest(transactionToString.getBytes());

        // converting to Hex String
        BigInteger num = new BigInteger(1, hashArray);
        StringBuilder hex = new StringBuilder(num.toString());
        while(hex.length() < 32) {
            hex.insert(0,'0');
        }
        return hex.toString();
    }

    // a method for printing the block details
    public String toString() {
        // creating a transactionList string
        String transactions = "";
        for(Transaction t : transactionList){
            transactions += t + "\n";
        }

        // creating an account balance map string
        String balanceMap = "";
        for (Map.Entry<String, Account> entry : accountBalanceMap.entrySet()) {
            balanceMap+= entry.getKey() + ":" + entry.getValue().getBalance() + "\n";
        }
        // returning block details
        return "Printing details of block number: " + this.blockNumber + "\nPrevious Hash: " + this.previousHash + "\nHash: " + this.hash
                + "\nTransactions : \n" + transactions +"Account Balances:\n"
                + balanceMap;
    }

    /**
     * Adds the account to accountBalanceMap
     * @param address - account ID
     * @param account - Account object
     */
    public void addAccount (String address, Account account){
        accountBalanceMap.put(address,account);
    }

    // Getters and Setters
    public Map<String, Account> getAccountBalanceMap() {
        return accountBalanceMap;
    }

    // used in Ledger to maintain the account balance map after committing the block
    public void setAccountBalanceMap(Map<String, Account> newAccountBalanceMap){
        this.accountBalanceMap = newAccountBalanceMap;
    }

    public List<Transaction> getTransactionList(){
        return new ArrayList<>(transactionList);
    }

    public int getBlockNumber() {
        return this.blockNumber;
    }

    public void setBlockNumber(int blockNumber) {
        this.blockNumber = blockNumber;
    }

    public Block getPreviousBlock() {
        return this.previousBlock;
    }

    public void setPreviousBlock(Block block) {
        this.previousBlock = block;
    }

    public String getHash() { return this.hash; }

    public void setPreviousHash(String previousHash) { this.previousHash = previousHash; }

    public void setHash(String hash) { this.hash = hash; }

    public String getPreviousHash() {
        if(getBlockNumber() == 1){
            return ""; //genesis
        }
        return this.previousHash;
    }

}