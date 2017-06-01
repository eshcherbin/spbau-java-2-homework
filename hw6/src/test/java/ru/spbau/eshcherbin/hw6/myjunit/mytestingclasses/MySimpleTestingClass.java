package ru.spbau.eshcherbin.hw6.myjunit.mytestingclasses;

import ru.spbau.eshcherbin.hw6.myjunit.MyTest;

public class MySimpleTestingClass {
    @MyTest
    public void simpleTest() {

    }

    @MyTest(ignore = "cuz i can")
    public void ignoredTest() {

    }

    @MyTest
    public void unexpectedExceptionTest() {
        throw new NullPointerException();
    }

    @MyTest(expected = NullPointerException.class)
    public void expectedExceptionTest() {
        throw new NullPointerException();
    }

    @MyTest(expected = NullPointerException.class)
    public void noExceptionTest() {

    }

    public void notATest() {

    }

    @MyTest(ignore = "well", expected = Exception.class)
    public static void staticNotATest() {

    }
}
