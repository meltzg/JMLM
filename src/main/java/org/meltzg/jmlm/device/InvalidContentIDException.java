package org.meltzg.jmlm.device;

/**
 * Used to represent invalid content ID states
 * @author Greg Meltzer
 * @author https://github.com/meltzg
 */
public class InvalidContentIDException extends Exception {
    public InvalidContentIDException(String msg) {
        super(msg);
    }
}