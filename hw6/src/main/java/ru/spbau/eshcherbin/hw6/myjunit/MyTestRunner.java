package ru.spbau.eshcherbin.hw6.myjunit;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * <tt>MyTestRunner</tt> class is used to run the annotated test methods.
 */
public class MyTestRunner {
    private static final @NotNull Logger logger = LoggerFactory.getLogger(MyTestRunner.class);

    private final @NotNull Class<?> clazz;
    private final @NotNull List<Method> testMethods;

    /**
     * Creates a test runner to run tests from the given class.
     * @param clazz the class to run the tests from.
     */
    public MyTestRunner(@NotNull Class<?> clazz) {
        this.clazz = clazz;
        testMethods = new ArrayList<>();
        for (Method method : clazz.getMethods()) {
            if (!Modifier.isStatic(method.getModifiers()) &&
                    Modifier.isPublic(method.getModifiers()) &&
                    method.getReturnType().equals(Void.class) &&
                    method.getParameterCount() == 0 &&
                    method.getAnnotation(MyTest.class) != null) {
                testMethods.add(method);
            }
        }
    }

    /**
     * Runs all the public void methods without arguments annotated with {@link MyTest} from the given class.
     */
    public @NotNull List<MyTestReport> runTests() throws InvalidTestException {
        List<MyTestReport> reports = new ArrayList<>();
        for (Method method : testMethods) {
            reports.add(runTestMethod(method));
        }
        return reports;
    }

    private @NotNull MyTestReport runTestMethod(@NotNull Method method)
            throws InvalidTestException {
        MyTest testAnnotation = method.getAnnotation(MyTest.class);
        if (!Objects.equals(testAnnotation.ignore(), "")) {
            return new MyTestIgnoredReport(clazz.getName(), method.getName(), testAnnotation.ignore(), 0);
        }
        Exception exception = null;
        Object instance;
        try {
            instance = clazz.newInstance();
        } catch (InstantiationException e) {
            logger.error("Couldn't instantiate test object of class {}, " +
                    "message: {}", clazz.getName(), e.getMessage());
            throw new InvalidTestException(e);
        } catch (IllegalAccessException e) {
            logger.error("IllegalAccessException while instantiating test object of class {}, " +
                    "message: {}", clazz.getName(), e.getMessage());
            throw new InvalidTestException(e);
        }
        long executionTimeStart= System.nanoTime();
        try {
            method.invoke(instance);
        } catch (IllegalAccessException e) {
            logger.error("IllegalAccessException while running test method {} from class {}, " +
                    "message: {}", method.getName(), clazz.getName(), e.getMessage());
            throw new InvalidTestException(e);
        } catch (InvocationTargetException e) {
            exception = (Exception) e.getCause();
        }
        long executionTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - executionTimeStart);
        if (exception == null && !testAnnotation.expected().equals(MyTest.NothingExpected.class)) {
            return new MyTestNoExceptionReport(
                    clazz.getName(),
                    method.getName(),
                    testAnnotation.expected(),
                    executionTime
            );
        }
        if (exception != null && !testAnnotation.expected().isInstance(exception)) {
            return new MyTestUnexpectedExceptionReport(clazz.getName(), method.getName(), exception, executionTime);
        }
        return new MyTestOkReport(clazz.getName(), method.getName(), executionTime);
    }
}
