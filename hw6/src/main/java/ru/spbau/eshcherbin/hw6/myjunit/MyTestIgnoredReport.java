package ru.spbau.eshcherbin.hw6.myjunit;

import org.jetbrains.annotations.NotNull;

/**
 * A result of a test run when the test is ignored.
 */
public class MyTestIgnoredReport extends MyTestReport {
    /**
     * Creates a report with given test run results.
     *
     * @param className the name of the test class
     * @param testName the name of the test method
     * @param ignoreReason the reason due to which the test was ignored
     * @param executionTime the execution time of the test run in milliseconds
     */
    protected MyTestIgnoredReport(@NotNull String className,
                                  @NotNull String testName,
                                  @NotNull String ignoreReason,
                                  long executionTime) {
        super(className,
                testName,
                "The test was ignored: " + ignoreReason,
                executionTime,
                true);
    }
}
