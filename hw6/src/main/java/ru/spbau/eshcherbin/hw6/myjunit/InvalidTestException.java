package ru.spbau.eshcherbin.hw6.myjunit;

import org.jetbrains.annotations.Nullable;

/**
 * Thrown when something is wrong with the test class or the test method.
 */
public class InvalidTestException extends Exception {
    public InvalidTestException(@Nullable Exception e) {
        super(e);
    }
}
