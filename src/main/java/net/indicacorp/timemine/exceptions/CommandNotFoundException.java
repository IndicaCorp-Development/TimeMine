package net.indicacorp.timemine.exceptions;

public class CommandNotFoundException extends Exception {
    public CommandNotFoundException() {
        super();
    }
    public CommandNotFoundException(String msg) {
        super(msg);
    }
}
