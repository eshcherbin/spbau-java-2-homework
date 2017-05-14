package ru.spbau.eshcherbin.hw6;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.spbau.eshcherbin.hw6.myjunit.InvalidTestException;
import ru.spbau.eshcherbin.hw6.myjunit.MyTestReport;
import ru.spbau.eshcherbin.hw6.myjunit.MyTestRunner;
import ru.spbau.eshcherbin.hw6.myjunit.MyTestUnexpectedExceptionReport;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Main class of the application.
 */
public class Main {
    private static final @NotNull Logger logger = LoggerFactory.getLogger(Main.class);

    /**
     * Starts the application.
     * Command line arguments should consist of one or more paths to the root directories of the test classes hierarchy
     * Note: it should lead to .class files rather than .java files.
     * @param args command line arguments
     */
    public static void main(String[] args) {
        final List<Class<?>> testClasses = new ArrayList<>();
        for (String argument : args) {
            try {
                testClasses.addAll(loadTestClasses(Paths.get(argument)));
            } catch (InvalidPathException e) {
                logger.error("Invalid path: ", argument);
                System.exit(1);
            } catch (IOException e) {
                logger.error("IOException: ", e.getMessage());
                e.printStackTrace();
                System.exit(1);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
        for (Class<?> testClass : testClasses) {
            try {
                final List<MyTestReport> reports = new MyTestRunner(testClass).runTests();
                if (reports.isEmpty()) {
                    continue;
                }
                System.out.println("Test reports from " + testClass.getName());
                long totalExecutionTime = 0;
                int testNumber = 0;
                int successfulCount = 0;
                for (MyTestReport report : reports) {
                    ++testNumber;
                    System.out.println();
                    System.out.println("Test #" + testNumber + ": " + report.getClassName() + "." + report.getTestName());
                    System.out.println("Status: " + (report.isSuccessful() ? "OK" : "FAILED"));
                    System.out.println("Time: " + report.getExecutionTime() + " ms");
                    System.out.println(report.getReportMessage());
                    if (report instanceof MyTestUnexpectedExceptionReport) {
                        ((MyTestUnexpectedExceptionReport) report).getException().printStackTrace(System.out);
                    }
                    totalExecutionTime += report.getExecutionTime();
                    if (report.isSuccessful()) {
                        ++successfulCount;
                    }
                }
                System.out.println();
                System.out.println(successfulCount + " out of " + reports.size() + " tests successful");
                System.out.println("Total execution time for tests from " + testClass.getName() + ": " +
                        totalExecutionTime + " ms");
            } catch (InvalidTestException e) {
                logger.error("InvalidTestException: {}", e.getMessage());
            }
        }
    }

    private static @NotNull List<Class<?>> loadTestClasses(@NotNull Path directory)
            throws IOException, ClassNotFoundException {
        final URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{directory.toUri().toURL()});
        final List<Path> testClassPaths = Files.walk(directory)
                .map(directory::relativize)
                .filter(path -> path.getFileName().toString().endsWith(".class"))
                .collect(Collectors.toList());
        final List<Class<?>> classes = new ArrayList<>();
        for (Path path : testClassPaths) {
            String className = path.toString().replace(File.separatorChar, '.');
            className = className.substring(0, className.lastIndexOf('.')); // get rid of the extension
            try {
                classes.add(urlClassLoader.loadClass(className));
            } catch (ClassNotFoundException e) {
                logger.error("Class not found: ", className);
            }
        }
        return classes;
    }
}
