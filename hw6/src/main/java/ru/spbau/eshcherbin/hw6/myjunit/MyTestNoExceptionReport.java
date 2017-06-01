package ru.spbau.eshcherbin.hw6.myjunit;

import org.jetbrains.annotations.NotNull;

/**
 * A result of a test run when no exception was thrown though it was expected.
 */
public class MyTestNoExceptionReport extends MyTestReport {
    /**
     * Creates a report with given test run results.
     *
     * @param className the name of the test class
     * @param testName the name of the test method
     * @param expectedExceptionClass the expected exception class
     * @param executionTime the execution time of the test run in milliseconds
     */
    protected MyTestNoExceptionReport(@NotNull String className,
                                              @NotNull String testName,
                                              @NotNull Class<? extends Exception> expectedExceptionClass,
                                              long executionTime) {
        super(className,
                testName,
                "Expected exception " + expectedExceptionClass.getName() + " was not thrown",
                executionTime,
                false);
    }
}

