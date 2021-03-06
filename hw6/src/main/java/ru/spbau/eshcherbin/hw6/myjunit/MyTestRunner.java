package ru.spbau.eshcherbin.hw6.myjunit;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
    private final @NotNull List<Method> beforeMethods;
    private final @NotNull List<Method> afterMethods;
    private final @NotNull List<Method> beforeClassMethods;
    private final @NotNull List<Method> afterClassMethods;

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
                    method.getReturnType().equals(Void.TYPE) &&
                    method.getParameterCount() == 0 &&
                    method.getAnnotation(MyTest.class) != null) {
                testMethods.add(method);
            }
        }
        beforeMethods = new ArrayList<>();
        for (Method method : clazz.getMethods()) {
            if (!Modifier.isStatic(method.getModifiers()) &&
                    Modifier.isPublic(method.getModifiers()) &&
                    method.getReturnType().equals(Void.TYPE) &&
                    method.getParameterCount() == 0 &&
                    method.getAnnotation(MyBefore.class) != null) {
                beforeMethods.add(method);
            }
        }
        afterMethods = new ArrayList<>();
        for (Method method : clazz.getMethods()) {
            if (!Modifier.isStatic(method.getModifiers()) &&
                    Modifier.isPublic(method.getModifiers()) &&
                    method.getReturnType().equals(Void.TYPE) &&
                    method.getParameterCount() == 0 &&
                    method.getAnnotation(MyAfter.class) != null) {
                afterMethods.add(method);
            }
        }
        beforeClassMethods = new ArrayList<>();
        for (Method method : clazz.getMethods()) {
            if (Modifier.isStatic(method.getModifiers()) &&
                    Modifier.isPublic(method.getModifiers()) &&
                    method.getReturnType().equals(Void.TYPE) &&
                    method.getParameterCount() == 0 &&
                    method.getAnnotation(MyBeforeClass.class) != null) {
                beforeClassMethods.add(method);
            }
        }
        afterClassMethods = new ArrayList<>();
        for (Method method : clazz.getMethods()) {
            if (Modifier.isStatic(method.getModifiers()) &&
                    Modifier.isPublic(method.getModifiers()) &&
                    method.getReturnType().equals(Void.TYPE) &&
                    method.getParameterCount() == 0 &&
                    method.getAnnotation(MyAfterClass.class) != null) {
                afterClassMethods.add(method);
            }
        }
    }

    /**
     * Runs all the public void methods without arguments annotated with {@link MyTest} from the given class.
     */
    public @NotNull List<MyTestReport> runTests()
            throws InvalidTestException, BeforeClassFailedException, AfterClassFailedException {
        List<MyTestReport> reports = new ArrayList<>();
        try {
            invokeAll(null, beforeClassMethods, "before class");
        } catch (InvocationTargetException e) {
            throw new BeforeClassFailedException(e);
        }
        for (Method method : testMethods) {
            reports.add(runTestMethod(method));
        }
        try {
            invokeAll(null, afterClassMethods, "after class");
        } catch (InvocationTargetException e) {
            throw new AfterClassFailedException(e);
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
        long executionTimeStart = System.nanoTime();
        try {
            invokeAll(instance, beforeMethods, "before methods");
            method.invoke(instance);
            invokeAll(instance, afterMethods, "after methods");
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

    /**
     * Invokes all methods from a list.
     * @param instance the instance which methods are invoked
     * @param methods the methods
     * @param methodType methods type description used for logging purposes
     */
    private void invokeAll(@Nullable Object instance,
                                  @NotNull List<Method> methods,
                                  @NotNull String methodType)
            throws InvalidTestException, InvocationTargetException {
        for (Method method : methods) {
            try {
                method.invoke(instance);
            } catch (IllegalAccessException e) {
                logger.error("IllegalAccessException while running {} method {} from class {}, " +
                        "message: {}", methodType, method.getName(), clazz.getName(), e.getMessage());
                throw new InvalidTestException(e);
            }
        }
    }
}
