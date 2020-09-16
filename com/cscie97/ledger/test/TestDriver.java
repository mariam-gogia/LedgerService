package com.cscie97.ledger.test;
import com.cscie97.ledger.*;
/**
 * TestDriver Class accepts a single command line parameter - command file
 * and calls the CommandProcessor
 *
 * @author Mariam Gogia
 */
public class TestDriver {
    public static void main(String[] args) throws CommandProcessorException {
        // check if the file path is passed as a command line argument
        String file;
        if(args.length > 0){
            file = args[0];
            // call to CommandProcessor to process the passed file
            CommandProcessor commandProcessor = new CommandProcessor();
            commandProcessor.processCommandFile(file);
        }
    }
}
