package ru.spbau.eshcherbin.hw6.myjunit;

import org.jetbrains.annotations.NotNull;

/**
 * A result of a test run when the test passed successfully.
 */
public class MyTestOkReport extends MyTestReport {
    /**
     * Creates a report with given test run results.
     *
     * @param className the name of the test class
     * @param testName the name of the test method
     * @param executionTime the execution time of the test run in milliseconds
     */
    protected MyTestOkReport(@NotNull String className,
                             @NotNull String testName,
                             long executionTime) {
        super(className, testName, "The test passed successfully", executionTime, true);
    }
}

