package ru.spbau.eshcherbin.hw6.myjunit.mytestingclasses;

import org.jetbrains.annotations.NotNull;
import ru.spbau.eshcherbin.hw6.myjunit.MyAfter;
import ru.spbau.eshcherbin.hw6.myjunit.MyBefore;
import ru.spbau.eshcherbin.hw6.myjunit.MyTest;

import java.util.LinkedList;
import java.util.List;

public class MyBeforeAfterTestingClass {
    @MyBefore
    public void before() {
        TestingHelper.LIST.add("before");
    }

    @MyTest
    public void test() {
        TestingHelper.LIST.add("test");
    }

    @MyAfter
    public void after() {
        TestingHelper.LIST.add("after");
    }

    public static class TestingHelper extends Exception {
        public static @NotNull List<String> LIST = new LinkedList<>();
    }
}
