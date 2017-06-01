package ru.spbau.eshcherbin.hw6.myjunit;

import org.jetbrains.annotations.NotNull;

/**
 * A result of a test run.
 */
public abstract class MyTestReport {
    private final @NotNull String className;
    private final @NotNull String testName;
    private final @NotNull String reportMessage;
    private final long executionTime;
    private final boolean isSuccessful;

    /**
     * Creates a report with given test run results.
     * @param className the name of the test class
     * @param testName the name of the test method
     * @param reportMessage the test run result message
     * @param executionTime the execution time of the test run in milliseconds
     * @param isSuccessful whether the test passed successfully
     */
    protected MyTestReport(@NotNull String className,
                           @NotNull String testName,
                           @NotNull String reportMessage,
                           long executionTime,
                           boolean isSuccessful) {
        this.className = className;
        this.testName = testName;
        this.reportMessage = reportMessage;
        this.executionTime = executionTime;
        this.isSuccessful = isSuccessful;
    }

    /**
     * Returns the name of the test class.
     * @return the name of the test class
     */
    public @NotNull String getClassName() {
        return className;
    }

    /**
     * Returns the name of the test method.
     * @return the name of the test method
     */
    public @NotNull String getTestName() {
        return testName;
    }

    /**
     * Returns the test run result message.
     * @return the test run result message
     */
    public @NotNull String getReportMessage() {
        return reportMessage;
    }

    /**
     * Returns the execution time of the test run in milliseconds.
     * @return the execution time of the test run in milliseconds
     */
    public long getExecutionTime() {
        return executionTime;
    }

    /**
     * Returns whether the test passed successfully.
     * @return whether the test passed successfully
     */
    public boolean isSuccessful() {
        return isSuccessful;
    }
}
