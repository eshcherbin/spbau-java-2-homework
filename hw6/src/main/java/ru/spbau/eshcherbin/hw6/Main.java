package ru.spbau.eshcherbin.hw6;

import ru.spbau.eshcherbin.hw6.myjunit.MyTestRunner;

/**
 * Main class of the application.
 */
public class Main {
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
        MyTestRunner.runTests(testClass);
    }
}
