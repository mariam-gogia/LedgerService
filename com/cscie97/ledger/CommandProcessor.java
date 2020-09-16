package com.cscie97.ledger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.*;

/**
 * CommandProcessor is an utility class for feeding
 * the LEdger a set of operations using command syntax
 *
 * @author Mariam Gogia
 */
public class CommandProcessor {
    // association
    private Ledger ledger;

    /** Processes a single command
     * @param command - command from the script
     * @throws CommandProcessorException - throws a CommandProcessor Exception
     */
    public void processCommand (String command) throws CommandProcessorException {
        // representing each line as a list of strings
        // where each position corresponds to one word in the line
        Pattern regex = Pattern.compile("[^\\s\"']+|\"[^\"]*\"|'[^']*'");
        Matcher regexMatcher = regex.matcher(command);

        List<String> commandTokens = new ArrayList<>();
        while (regexMatcher.find()) {
            commandTokens.add(regexMatcher.group());
        }

        // commandTokens.get(0) is the line number, thus start from position 1
        String cmd = commandTokens.get(1);
        try {
            switch (cmd) {
                case "create-ledger":
                    // properly written create-ledger command should have 7 elements
                    // lineNumber create-ledger <name> description <description> seed <seed>
                    if(commandTokens.size() != 7){
                        throw new CommandProcessorException(cmd, "invalid format", Integer.parseInt(commandTokens.get(0)));
                    }
                    // corresponding indices for 'name' 'description' 'seed' are at positions 2,4,6 in commandTokens list
                    if(ledger == null){
                        ledger = new Ledger(commandTokens.get(2), commandTokens.get(4), commandTokens.get(6));
                    }
                    else {
                        throw new LedgerException("createLedger" ,"Ledger Service already initialized");
                    }

                    break;

                case "create-account":
                    assert ledger != null;
                    if(commandTokens.size() != 3){
                        throw new CommandProcessorException(cmd, "invalid format", Integer.parseInt(commandTokens.get(0)));
                    }
                    // corresponding index for 'account-id' is at position 2
                    System.out.println("Created Account: " + ledger.createAccount(commandTokens.get(2)));
                    break;

                case "process-transaction":
                    assert ledger != null;
                    if(commandTokens.size() != 13){
                        throw new CommandProcessorException(cmd, "invalid format", Integer.parseInt(commandTokens.get(0)));
                    }
                    // checking if payer and receiver accounts exist before passing
                    if(ledger.getCurrentBlock().getAccountBalanceMap().get(commandTokens.get(10)) == null ||
                            ledger.getCurrentBlock().getAccountBalanceMap().get(commandTokens.get(12)) == null){
                        throw new LedgerException("processTransaction", "Payer or Receiver does not exist");
                    } else {
                        String transactionID = ledger.processTransaction(
                                new Transaction(
                                        commandTokens.get(2),
                                        Integer.parseInt(commandTokens.get(4)),
                                        Integer.parseInt(commandTokens.get(6)),
                                        commandTokens.get(8),
                                        ledger.getCurrentBlock().getAccountBalanceMap().get(commandTokens.get(10)),
                                        ledger.getCurrentBlock().getAccountBalanceMap().get(commandTokens.get(12))
                                )
                        );
                        System.out.println("processedTransaction: " + transactionID);
                    }
                    break;

                case "get-account-balance":
                    assert ledger != null;
                    if(commandTokens.size() != 3){
                        throw new CommandProcessorException(cmd, "invalid format", Integer.parseInt(commandTokens.get(0)));
                    }
                    final int accountBalance = ledger.getAccountBalance(commandTokens.get(2));
                    System.out.println("Account Balance " +commandTokens.get(2) +": " + accountBalance);
                    break;

                case "get-account-balances":
                    assert ledger != null;
                    if(commandTokens.size() != 2){
                        throw new CommandProcessorException(cmd, "invalid format", Integer.parseInt(commandTokens.get(0)));
                    }
                    Map<String, Integer> accountBalances = ledger.getAccountBalances();
                    System.out.println("Printing the account balance map: ");
                    accountBalances.forEach((k, v) -> {
                        System.out.println(k + " - " + v);
                    });
                    break;

               case "get-block":
                    assert ledger != null;
                   if(commandTokens.size() != 3){
                       throw new CommandProcessorException(cmd, "invalid format", Integer.parseInt(commandTokens.get(0)));
                   }
                    Block block = ledger.getBlock(Integer.parseInt(commandTokens.get(2)));
                    System.out.println(block.toString());
                    break;

               case "get-transaction":
                    assert ledger != null;
                   if(commandTokens.size() != 3){
                       throw new CommandProcessorException(cmd, "invalid format", Integer.parseInt(commandTokens.get(0)));
                   }
                    System.out.println(ledger.getTransaction(commandTokens.get(2)));
                    break;

               case "validate":
                    assert ledger != null;
                    if(commandTokens.size() != 2){
                        throw new CommandProcessorException(cmd, "invalid format", Integer.parseInt(commandTokens.get(0)));
                    }
                    ledger.validate();
                    break;

               default:
                    throw new CommandProcessorException(cmd, "Invalid Command", Integer.parseInt(commandTokens.get(0)));
            }
        } catch (LedgerException e) {
           System.out.println("Ledger Exception: " + e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            throw new CommandProcessorException(cmd, "No such algorithm", Integer.parseInt(commandTokens.get(0)));
        }
    }

    /** Processes a set of commands provided within the given commandFile
     * @param commandFile - script
     * @throws CommandProcessorException - throws a CommandProcessorException
     */
    public void processCommandFile (String commandFile) throws CommandProcessorException {
        // check if the file exists
        File file = new File(commandFile);
        if(!file.isFile()) {
            throw new CommandProcessorException("processCommandFile", "File does not exist", 0);
        }

        // read and add lines to commandFileLines list
        List<String> commandFileLines = new ArrayList<String>();
        try(Stream<String> stream = Files.lines((Paths.get(commandFile)))) {
            stream.forEach(commandFileLines::add);
        }
        catch (IOException e){
            throw new CommandProcessorException("processCommandFile", "Error reading the file", 0);
        }

        // Using sorted tree to add
        // filtering out comments and empty lines
        TreeMap<Integer, String> commands = new TreeMap<Integer, String>();
        for(int i = 0; i < commandFileLines.size(); i++) {
            if(commandFileLines.get(i).contains("#") || commandFileLines.get(i).length() == 0) {
                continue;
            }
            // line 0 is line 1
            commands.put(i+1, commandFileLines.get(i));
        }

        for(Map.Entry<Integer, String> entry : commands.entrySet()) {
            // format command string to include the line number
            // lineNumber command inputs
            processCommand(String.format("%d %s", entry.getKey(), entry.getValue()));
        }
    }
}
