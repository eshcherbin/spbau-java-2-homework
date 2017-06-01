package ru.spbau.eshcherbin.hw6.myjunit;

import org.jetbrains.annotations.Nullable;

/**
 * Thrown when a {@link MyBeforeClass} annotated method throws an exception.
 */
public class BeforeClassFailedException extends Exception {
    public BeforeClassFailedException(@Nullable Exception e) {
        super(e);
    }
}
