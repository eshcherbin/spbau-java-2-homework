package ru.spbau.eshcherbin.hw6;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.spbau.eshcherbin.hw6.myjunit.InvalidTestException;
import ru.spbau.eshcherbin.hw6.myjunit.MyTestReport;
import ru.spbau.eshcherbin.hw6.myjunit.MyTestRunner;
import ru.spbau.eshcherbin.hw6.myjunit.MyTestUnexpectedExceptionReport;

import java.util.List;

/**
 * Main class of the application.
 */
public class Main {
    private static final @NotNull Logger logger = LoggerFactory.getLogger(Main.class);

    /**
     * Starts the application.
     * @param args command line arguments
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Wrong number of arguments");
            System.exit(1);
        }
        String testClassName = args[0];
        Class<?> testClass;
        try {
            testClass = Class.forName(testClassName);
        } catch (ClassNotFoundException e) {
            System.err.println("Class not found : " + testClassName);
            System.exit(1);
            return;
        }
        try {
            System.out.println("Running tests from " + testClassName);
            final List<MyTestReport> reports = new MyTestRunner(testClass).runTests();
            long totalExecutionTime = 0;
            int testNumber = 0;
            for (MyTestReport report : reports) {
                ++testNumber;
                System.out.println();
                System.out.println("Test #" + testNumber + ": " + report.getClassName() + "." + report.getTestName());
                System.out.println("Status: " + (report.isSuccessful() ? "OK" : "FAILED"));
                System.out.println("Time: " + report.getExecutionTime() + " ms");
                System.out.println(report.getReportMessage());
                if (report instanceof MyTestUnexpectedExceptionReport) {
                    ((MyTestUnexpectedExceptionReport) report).getException().printStackTrace();
                }
                totalExecutionTime += report.getExecutionTime();
            }
            System.out.println();
            System.out.println("Total execution time for tests from " + testClassName + ": " +
                    totalExecutionTime + " ms");
        } catch (InvalidTestException e) {
            logger.error("InvalidTestException: {}", e.getMessage());
        }
    }
}
