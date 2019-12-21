package net.indicacorp.timemine.exceptions;

public class InvalidWorldException extends Exception {

    public InvalidWorldException(long id) {
        super("TimeMine block (ID: " + id + ") contains an invalid world name");
    }
}
