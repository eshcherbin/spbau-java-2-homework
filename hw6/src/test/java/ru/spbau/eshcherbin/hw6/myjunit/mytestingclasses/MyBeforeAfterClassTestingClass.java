package ru.spbau.eshcherbin.hw6.myjunit.mytestingclasses;

import org.jetbrains.annotations.NotNull;
import ru.spbau.eshcherbin.hw6.myjunit.MyAfterClass;
import ru.spbau.eshcherbin.hw6.myjunit.MyBeforeClass;
import ru.spbau.eshcherbin.hw6.myjunit.MyTest;

import java.util.LinkedList;
import java.util.List;

public class MyBeforeAfterClassTestingClass {
    @MyBeforeClass
    public static void beforeClass() {
        TestingHelper.LIST.add("beforeClass");
    }

    @MyTest
    public void test1() {
        TestingHelper.LIST.add("test1");
    }

    @MyTest
    public void test2() {
        TestingHelper.LIST.add("test2");
    }

    @MyAfterClass
    public static void afterClass() {
        TestingHelper.LIST.add("afterClass");
    }

    public static class TestingHelper extends Exception {
        public static @NotNull List<String> LIST = new LinkedList<>();
    }
}
