package ru.spbau.eshcherbin.hw6.myjunit;

import org.jetbrains.annotations.Nullable;

/**
 * Thrown when a {@link MyAfterClass} annotated method throws an exception.
 */
public class AfterClassFailedException extends Exception {
    public AfterClassFailedException(@Nullable Exception e) {
        super(e);
    }
}
