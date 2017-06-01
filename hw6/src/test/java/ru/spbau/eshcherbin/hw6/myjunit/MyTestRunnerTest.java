package ru.spbau.eshcherbin.hw6.myjunit;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import ru.spbau.eshcherbin.hw6.myjunit.mytestingclasses.MyBeforeAfterClassTestingClass;
import ru.spbau.eshcherbin.hw6.myjunit.mytestingclasses.MyBeforeAfterTestingClass;
import ru.spbau.eshcherbin.hw6.myjunit.mytestingclasses.MyInvalidTestingClassWithConstructorWithArguments;
import ru.spbau.eshcherbin.hw6.myjunit.mytestingclasses.MySimpleTestingClass;

import java.util.Comparator;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class MyTestRunnerTest {
    @Test
    public void simpleTest() throws Exception {
        final MyTestRunner testRunner = new MyTestRunner(MySimpleTestingClass.class);
        final List<MyTestReport> reports = testRunner.runTests();
        reports.sort(Comparator.comparing(MyTestReport::getTestName));
        assertThat(reports.size(), is(5));

        final MyTestReport report0 = reports.get(0);
        assertThat(report0.getClassName(), is(MySimpleTestingClass.class.getName()));
        assertThat(report0.getTestName(), is("expectedExceptionTest"));
        assertThat(report0 instanceof MyTestOkReport, is(true));
        assertThat(report0.isSuccessful(), is(true));

        final MyTestReport report1 = reports.get(1);
        assertThat(report1.getClassName(), is(MySimpleTestingClass.class.getName()));
        assertThat(report1.getTestName(), is("ignoredTest"));
        assertThat(report1 instanceof MyTestIgnoredReport, is(true));
        assertThat(report1.isSuccessful(), is(true));

        final MyTestReport report2 = reports.get(2);
        assertThat(report2 instanceof MyTestNoExceptionReport, is(true));
        assertThat(report2.getClassName(), is(MySimpleTestingClass.class.getName()));
        assertThat(report2.getTestName(), is("noExceptionTest"));
        assertThat(report2.isSuccessful(), is(false));

        final MyTestReport report3 = reports.get(3);
        assertThat(report3.getClassName(), is(MySimpleTestingClass.class.getName()));
        assertThat(report3.getTestName(), is("simpleTest"));
        assertThat(report3 instanceof MyTestOkReport, is(true));
        assertThat(report3.isSuccessful(), is(true));

        final MyTestReport report4 = reports.get(4);
        assertThat(report4.getClassName(), is(MySimpleTestingClass.class.getName()));
        assertThat(report4.getTestName(), is("unexpectedExceptionTest"));
        assertThat(report4 instanceof MyTestUnexpectedExceptionReport, is(true));
        assertThat(report4.isSuccessful(), is(false));
        assertThat(((MyTestUnexpectedExceptionReport) report4).getException().getClass(),
                is(NullPointerException.class));
    }

    @Test(expected = InvalidTestException.class)
    public void constructorWithArgumentsTest() throws Exception {
        final MyTestRunner testRunner = new MyTestRunner(MyInvalidTestingClassWithConstructorWithArguments.class);
        testRunner.runTests();
    }

    @Test
    public void beforeAfterTest() throws Exception {
        final MyTestRunner testRunner = new MyTestRunner(MyBeforeAfterTestingClass.class);
        testRunner.runTests();
        assertThat(MyBeforeAfterTestingClass.TestingHelper.LIST,
                is(ImmutableList.of("before", "test", "after")));
    }

    @Test
    public void beforeAfterClassTest() throws Exception {
        final MyTestRunner testRunner = new MyTestRunner(MyBeforeAfterClassTestingClass.class);
        testRunner.runTests();
        assertThat(MyBeforeAfterClassTestingClass.TestingHelper.LIST,
                is(ImmutableList.of("beforeClass", "test1", "test2", "afterClass")));
    }
}