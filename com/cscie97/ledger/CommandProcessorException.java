package com.cscie97.ledger;
/**
 * CommandProcessorException captures the command that was attempted and the reason for failure.
 * It includes the line number of the script causing the error
 *
 * @author Mariam Gogia
 */
public class CommandProcessorException extends Exception {

    // CommandProcessorException class properties
    private String command;
    private String reason;
    private int lineNumber;


    /** CommandProcessorException constructor
     * @param command - command causing the exception
     * @param reason - the reason for exception
     * @param lineNumber - the line number of the script causing the exception
     */
    public CommandProcessorException(String command, String reason, int lineNumber){
        super(command + ": " + reason + ", line: " + lineNumber); //maybe
        this.command = command;
        this.reason = reason;
        this.lineNumber = lineNumber;
    }

    // Getters and Setters
    public String getReason() {
        return this.reason;
    }

    public String getAction() {
        return this.command;
    }

    public int getLineNumber() { return this.lineNumber; }

    public void setCommand(String command) { this.command = command; }

    public void setReason(String reason) { this.reason = reason; }

    public void setLineNumber(int lineNumber) { this.lineNumber = lineNumber;}

    public String getCommand() { return command; }
}