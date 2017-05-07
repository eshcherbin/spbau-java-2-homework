package ru.spbau.eshcherbin.hw6.myjunit;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * <tt>MyTestRunner</tt> class is used to run the annotated test methods.
 */
public class MyTestRunner {
    /**
     * Runs all the static methods annotated with {@link MyTest} from the given class.
     * @param clazz the class that contains the test methods
     */
    public static void runTests(@NotNull Class<?> clazz) {
        for (Method method : clazz.getMethods()) {
            if (!Modifier.isStatic(method.getModifiers())) {
                continue; // test method should be static
            }
            method.invoke()
        }
    }
}
