package framework.reporting.listeners;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import framework.BaseUtilities;
import framework.driverfactory.DriverFactory;
import framework.reporting.extent.ExtentManager;
import org.apache.commons.lang3.SystemUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.*;
import org.testng.xml.XmlSuite;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static framework.reporting.extent.ExtentTestManager.getTest;
import static framework.reporting.extent.ExtentTestManager.startTest;

public class ExtentReportListener extends BaseUtilities implements ITestListener, IReporter {
    List<String> testRunnerOutput;
    private static ExtentReports reporter;
    private Map<String, String> systemInfo;
    private ExtentSparkReporter sparkReporter;

    public ExtentReportListener() {
        String getDate = timestamp("yyyy-MM-dd");
        String getTime = timestamp("HHmmss");

        testRunnerOutput = new ArrayList<>();
        String reportPathStr = System.getProperty("reportPath");
        File reportPath;

        try {
            reportPath = new File(reportPathStr);
        } catch (NullPointerException e) {
            reportPath = new File(TestNG.DEFAULT_OUTPUTDIR + "/reports/" + getDate);
        }

        if (!reportPath.exists()) {
            if (!reportPath.mkdirs()) {
                throw new RuntimeException("Failed to create output run directory");
            }
        }

        File reportFile = new File(reportPath, "report_"+ getDate + "_" + getTime +".html");

        sparkReporter = new ExtentSparkReporter(reportFile);
//        reporter = new ExtentReports();
        ExtentManager.extentReports.attachReporter(sparkReporter);
    }

    /**
     * Gets the system information map
     *
     * @return The system information map
     */
    public Map<String, String> getSystemInfo() {
        return systemInfo;
    }

    /**
     * Sets the system information
     *
     * @param systemInfo The system information map
     */
    public void setSystemInfo(Map<String, String> systemInfo) {
        this.systemInfo = systemInfo;
    }

    private static String getTestMethodName(ITestResult iTestResult) {
        return iTestResult.getMethod().getConstructorOrMethod().getName();
    }

    private static String getTestMethodDescription(ITestResult iTestResult) {
        return iTestResult.getMethod().getDescription();
    }
    @Override
    public void onStart(ITestContext iTestContext) {
//        iTestContext.setAttribute("WebDriver", DriverFactory.getDriver());

    }
    @Override
    public void onFinish(ITestContext iTestContext) {
        //Do tier down operations for ExtentReports reporting!
        ExtentManager.extentReports.flush();
    }
    @Override
    public void onTestStart(ITestResult iTestResult) {
        startTest(getTestMethodName(iTestResult), "<strong>BROWSER:</strong> " + iTestResult.getMethod().getXmlTest().getParameter("browser_to_run_tests") + "" +
                "<br /><strong>TEST:</strong> " + getTestMethodDescription(iTestResult) +
                "<br /><strong>RUN LOCATION:</strong> " + iTestResult.getMethod().getXmlTest().getParameter("run_test_local_or_grid") +
                "<br /><strong>URL:</strong> " + iTestResult.getMethod().getXmlTest().getParameter("url_to_execute_tests") +
                "<br /><strong>OPERATING SYSTEM:</strong> " + SystemUtils.OS_NAME);
    }

    @Override
    public void onTestFailure(ITestResult iTestResult) {
        print(getTestMethodName(iTestResult) + " test is failed.");
        //Get driver from BaseTest and assign to local webdriver variable.
        Object testClass = iTestResult.getInstance();
        WebDriver driver = ((DriverFactory) testClass).getDriver();
        //Take base64Screenshot screenshot for extent reports
        String base64Screenshot =
                "data:image/png;base64," + ((TakesScreenshot) Objects.requireNonNull(driver)).getScreenshotAs(OutputType.BASE64);
        //ExtentReports log and screenshot operations for failed tests.
        getTest().log(Status.FAIL, getTestMethodName(iTestResult) + " Test Failed",
                getTest().addScreenCaptureFromBase64String(base64Screenshot).getModel().getMedia().get(0));
    }
    @Override
    public void onTestSkipped(ITestResult iTestResult) {
        print(getTestMethodName(iTestResult) + " test is skipped.");
        //ExtentReports log operation for skipped tests.
        getTest().log(Status.SKIP, getTestMethodName(iTestResult) + " Test Skipped");
    }
    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult iTestResult) {
        print("Test failed but it is in defined success ratio " + getTestMethodName(iTestResult));
    }


    public void generateReport(List<XmlSuite> list, List<ISuite> list1, String s) {
        System.out.println("this should generate the report");
        reporter.setSystemInfo("Operating System", System.getProperty("os.name"));
        if (getSystemInfo() != null) {
            for (Map.Entry<String, String> entry : getSystemInfo().entrySet()) {
                reporter.setSystemInfo(entry.getKey(), entry.getValue());
            }
        }
        reporter.addTestRunnerOutput(testRunnerOutput);
    }
}