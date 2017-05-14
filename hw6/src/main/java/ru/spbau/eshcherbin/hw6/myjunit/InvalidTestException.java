package ru.spbau.eshcherbin.hw6.myjunit;

/**
 * Thrown when something is wrong with the test class or the test method.
 */
public class InvalidTestException extends Exception {
    public InvalidTestException(Exception e) {
        super(e);
    }
}
