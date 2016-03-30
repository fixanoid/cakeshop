package com.jpmorgan.ib.caonpd.ethereum.enterprise.test.config;

import org.apache.commons.lang3.StringUtils;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

public class CleanConsoleListener extends TestListenerAdapter {

    @Override
    public void beforeConfiguration(ITestResult tr) {
        super.beforeConfiguration(tr);
        // uncomment for extra debugging
        // System.out.println();
        // System.out.println("### RUNNING " + tr.getName());
        // System.out.println();
    }

    @Override
    public void onTestStart(ITestResult result) {
        super.onTestStart(result);

        String log = "# START:  " + result.getName() + " #";

        System.out.println();
        System.out.println(StringUtils.repeat("#", log.length()));

        System.out.println(log);

        System.out.println(StringUtils.repeat("#", log.length()));
        System.out.println();
    }

    @Override
    public void onTestFailure(ITestResult tr) {
        super.onTestFailure(tr);
        testEnd(tr);
    }

    @Override
    public void onTestSkipped(ITestResult tr) {
        super.onTestSkipped(tr);
        testEnd(tr);
    }

    @Override
    public void onTestSuccess(ITestResult tr) {
        super.onTestSuccess(tr);
        testEnd(tr);
    }

    private void testEnd(ITestResult result) {
        System.out.println();
        System.out.println("### END " + result.getName() + " (" + (result.isSuccess() ? "PASS" : "FAIL") + ") " + " ###");
        System.out.println();
    }


}
