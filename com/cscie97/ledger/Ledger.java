package com.cscie97.ledger;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Ledger class supports Ledger Service for the blockchain
 * It manages the Blocks, processes transactions, creates accounts, and validates the blockchain
 * It is the API used by clients of the Ledger
 *
 * @author Mariam Gogia
 */
public class Ledger {

    // Ledger class properties & associations
    private String name;
    private String description;
    private String seed; // used as input for hashing algorithm;
    private Block genesisBlock; // the first block of the blockchain
    private Block currentBlock; //current uncommitted block

    // blockMap is implemented as a TreeMap which is a sorted tree
    public  TreeMap<Integer, Block> blockMap = new TreeMap<Integer, Block>();

    /**
     * To initialize ledger, the user must provide:
     * @param name - the name of the ledger
     * @param description - ledger description
     * @param seed - ledger seed
     */
    // Ledger constructor  - Initializing ledger
    public Ledger(String name, String description, String seed) throws LedgerException {
        if(name == null || description == null || seed == null){
            throw new LedgerException("Ledger Initialization Failed ", "Parameter(s) cannot be null");
        }
        // initializing the first block and a master account
        genesisBlock = new Block(1, "");
        Account masterAccount = new Account("master", Integer.MAX_VALUE);
        genesisBlock.addAccount("master", masterAccount);

        currentBlock = genesisBlock;
        this.name = name;
        this.description = description;
        this.seed = seed;
    }

    /**
     * Create a new account, assign a unique ID,
     * set balance to 0.
     * @param accountID - account ID
     * @return accountID
     * @throws LedgerException - Throws a Ledger Exception
     */
    public String createAccount(String accountID) throws LedgerException {
        // check if such account already exists
        if(currentBlock.getAccountBalanceMap().containsKey(accountID)){
            throw new LedgerException("createAccount " +accountID , "The account already exists");
        }
        // if the account does not exist - create it and add to the map maintained by the Block class
        Account newAccount = new Account(accountID, 0);
        currentBlock.addAccount(accountID,newAccount);
        return accountID;
    }

    /**
     * Validate the transaction, if valid - add it to the current block
     * update the accountBalanceMap.
     * @param transaction - takes in transaction
     * @return transactionID
     * @throws LedgerException - throws a Ledger Exception
     */
    public String processTransaction(Transaction transaction) throws LedgerException{
        // validating the transaction using  a helper method
        if(isValidTransaction(transaction)){
            // deduct the transaction amount + fee from payer's account
            transaction.getPayer().setBalance(transaction.getPayer().getBalance()-(transaction.getAmount()+transaction.getFee()));

            // add amount to receiver's account
            transaction.getReceiver().setBalance(transaction.getReceiver().getBalance() + transaction.getAmount());

            // add fee amount to master's account
            int masterAmount = currentBlock.getAccountBalanceMap().get("master").getBalance();
            currentBlock.getAccountBalanceMap().get("master").setBalance(masterAmount+transaction.getFee());

            // check if the block is full and if so, add to blockchain
            boolean blockFull = currentBlock.addTransaction(transaction);
            if (blockFull) {
                addBlock(currentBlock);
            }
            return transaction.getTransactionID();
        }
        else {
            throw new LedgerException("processTransaction", "Transaction Invalid");
        }
    }

    // helper method for processTransaction check the validity of the transaction
    private boolean isValidTransaction (Transaction transaction){
        // the existence of the payer and receiver accounts is checked in
        // CommandProcessor before passing the Account references.

        // check for valid fee amount
        if(transaction.getFee() < 10){
            return false;
        }
        // check for valid amount
        if(transaction.getAmount() < 0){
            return false;
        }
        // check for valid note length
        if(transaction.getNote().length() > 1024){
            return false;
        }
        // check if the payer has sufficient funds for the transaction
        if(transaction.getPayer().getBalance() < (transaction.getAmount() + transaction.getFee() )){
            return false;
        }
        return true;
    }

    /**
     * Based on the most recent completed block -
     * gets the account balance given the accountID
     * @param address - account ID
     * @return account balance
     * @throws LedgerException throws a Ledger Exception
     */
    public int getAccountBalance(String address) throws LedgerException {
        // check if there is a completed block at all
        if (blockMap.firstEntry() != null) {

            // check if the account with given address exists
            if (!blockMap.lastEntry().getValue().getAccountBalanceMap().containsKey(address)) {
                throw new LedgerException("getAccountBalance(id)", "No such account");
            } else {
                return blockMap.lastEntry().getValue().getAccountBalanceMap().get(address).getBalance();
            }
        }
        else {
            throw new LedgerException("getAccountBalance(id)" ,"No committed block");
        }
    }


    /**
     * Gets account balances for all accounts in the most recent completed block
     * @return map with accounts and their balances
     * @throws LedgerException throws a Ledger Exception
     */
    public Map<String, Integer> getAccountBalances() throws LedgerException{
        // check if there is a completed block at all
        if (blockMap.firstEntry() != null) {

            // create a new map with accounts and balances
            Map<String, Integer> accountBalances = new HashMap<String, Integer>();
            for (Map.Entry<String, Account> entry : blockMap.lastEntry().getValue().getAccountBalanceMap().entrySet()) {
                String accountID = entry.getKey();
                int balance = entry.getValue().getBalance();
                accountBalances.put(accountID, balance);
            }
            return accountBalances;
        } else {
            throw new LedgerException("getAccountBalances", "No committed blocks");
        }
    }

    /**
     * Get block for the given block number
     * @param blockNumber - block number
     * @return Block
     * @throws LedgerException throws a Ledger Exception
     */
    public Block getBlock (int blockNumber) throws LedgerException {
        // check if such block exists and return the block
        if(blockMap.get(blockNumber) != null){
            return blockMap.get(blockNumber);
        }
        else {
            throw new LedgerException("getBlock(id)",  "Block " + blockNumber + " does not exist");
        }
    }

    /**
     * Adds block to the blockchain
     * @param block  - block to be added
     */
    private void addBlock (Block block){
        // adding the first block
        if(block.getBlockNumber() == 1){
            blockMap.put(1, block);
        }

        // adding all other blocks
        else {
            blockMap.put(blockMap.lastKey() + 1, block);
        }

        // making a deep copy of the accountBalanceMap
        Map<String, Account> accountBalanceMapDeepCopy = new HashMap<String, Account>();
        for(Map.Entry<String, Account> entry: block.getAccountBalanceMap().entrySet()){
            accountBalanceMapDeepCopy.put(entry.getKey(), new Account(entry.getValue()));
        }

        // getting the hash of the block
        String hash = null;
        try {
            hash = block.getHash(this.seed);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        // creating a new block by passing the block number and hash for its previousHash field
        // setting the accountBalanceMap to the deep copy and setting the current block to a previous block of the new block
        if (hash != null) {
            currentBlock = new Block(blockMap.lastKey() + 1, hash);
            currentBlock.setAccountBalanceMap(accountBalanceMapDeepCopy);
            currentBlock.setPreviousBlock(block);
        }
    }

    /**
     * Returns the transaction for the given block
     * @param transactionID - unique transaction ID
     * @return Transaction
     */
    public Transaction getTransaction(String transactionID) throws LedgerException {
        // iterating through blockchain to find the transaction
        // starting from the current block and going backwards
        Block iterator = currentBlock;
        while (iterator != null) {
            for (Transaction transaction : iterator.transactionList) {
                if(transaction.getTransactionID().equals(transactionID)){
                    return transaction;
                }
            }
            iterator = iterator.getPreviousBlock();
        }
        throw new LedgerException("getTransaction(id) " , "Transaction does not exist");
    }

    /** Validating the state of blockchain
     * confirming the hashes of each blocks are correct,
     * and that they are correctly chained,
     * validating the total Units in the system
     * validating that blocks contain correct amount of transactions
     * @throws LedgerException throws a Ledger Exception
     * @throws NoSuchAlgorithmException throws a NoSuchAlgorithmException
     */
    public void validate () throws LedgerException, NoSuchAlgorithmException {
        if(blockMap.firstEntry() == null){
            throw new LedgerException("validate()" , "No committed blocks - nothing to validate");
        }
        // checking blockchain total balance
        int totalUnits = 0;
        for (Map.Entry<String, Account> entry : currentBlock.getAccountBalanceMap().entrySet()) {
            totalUnits += entry.getValue().getBalance(); //summing the balances
        }
        if (totalUnits != Integer.MAX_VALUE) {
            throw new LedgerException("validate()", "The total balance does not match the max value");
        }
        //checking if each committed block has exactly 10 transactions
        for (Map.Entry<Integer, Block> entry : blockMap.entrySet()) {
            if (entry.getValue().transactionList.size() != 10) {
                throw new LedgerException("validate()", " Block number: " + entry.getValue().getBlockNumber() + "Does not contain 10 transactions");
            }
        }
        // Checking the hashes - Recomputing the hash of the current block
        // and comparing it to the previousHash of the next block
        // Starting from block 1
        for (int i = 1; i < blockMap.size(); i++) {
            if (blockMap.get(i).getHash(getSeed()).equals(blockMap.get(i + 1).getPreviousHash())) {
            } else {
                throw new LedgerException("validate() ", "Hashes do not match");
            }
        }
    }

    // Getters and Setters
    // most of them are unused, however implemented as per design document
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSeed() {
        return seed;
    }

    public void setSeed(String seed) {
        this.seed = seed;
    }

    public Block getCurrentBlock() {
        return currentBlock;
    }

    public Block getGenesisBlock() { return genesisBlock; }

    public TreeMap<Integer, Block> getBlockMap() { return blockMap; }

    public void setGenesisBlock(Block genesisBlock) { this.genesisBlock = genesisBlock; }

    public void setCurrentBlock(Block currentBlock) { this.currentBlock = currentBlock; }

    public void setBlockMap(TreeMap<Integer, Block> blockMap) { this.blockMap = blockMap; }

}
