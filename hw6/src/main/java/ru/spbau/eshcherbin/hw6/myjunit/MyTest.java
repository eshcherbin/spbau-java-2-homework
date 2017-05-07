package ru.spbau.eshcherbin.hw6.myjunit;

import org.jetbrains.annotations.Nullable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to a test method.
 * Annotated method should be <tt>public</tt> and <tt>static</tt> and have no arguments.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MyTest {
    /**
     * Tells the test runner to ignore this test method with the specified reason.
     */
    @Nullable String ignore() default "";
}
