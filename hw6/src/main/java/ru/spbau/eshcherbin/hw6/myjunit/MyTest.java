package ru.spbau.eshcherbin.hw6.myjunit;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to a test method.
 * Annotated method should be <tt>public</tt> and have no arguments.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MyTest {
    /**
     * Tells the test runner to ignore this test method with the specified reason.
     */
    @NotNull String ignore() default "";

    /**
     * Tells the test runner to expect the specified exception thrown while running this test.
     */
    @NotNull Class<? extends Exception> expected() default NothingExpected.class;

    /**
     * Special class that is used to specify that no exception is expected.
     */
    class NothingExpected extends Exception {
    }
}
