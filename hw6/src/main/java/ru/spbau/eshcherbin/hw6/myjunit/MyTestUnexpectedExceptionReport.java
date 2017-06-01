package ru.spbau.eshcherbin.hw6.myjunit;

import org.jetbrains.annotations.NotNull;

/**
 * A result of a test run when an unexpected exception was thrown.
 */
public class MyTestUnexpectedExceptionReport extends MyTestReport {
    private @NotNull Exception exception;

    /**
     * Creates a report with given test run results.
     *
     * @param className the name of the test class
     * @param testName the name of the test method
     * @param exception the unexpected exception
     * @param executionTime the execution time of the test run in milliseconds
     */
    protected MyTestUnexpectedExceptionReport(@NotNull String className,
                                              @NotNull String testName,
                                              @NotNull Exception exception,
                                              long executionTime) {
        super(className,
                testName,
                "An unexpected exception was caught: " + exception.getClass().getName() +
                        (exception.getMessage() == null ||  exception.getMessage().isEmpty()
                                ? ""
                                : " (" + exception.getMessage() + ")"),
                executionTime,
                false);
        this.exception = exception;
    }

    /**
     * Returns the unexpected exception that was thrown.
     * @return the unexpected exception
     */
    public @NotNull Exception getException() {
        return exception;
    }
}

