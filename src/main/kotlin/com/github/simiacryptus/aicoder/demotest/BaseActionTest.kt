package com.github.simiacryptus.aicoder.demotest

import com.github.simiacryptus.aicoder.demotest.PlanAheadActionTest.Companion
import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.fixtures.CommonContainerFixture
import com.intellij.remoterobot.fixtures.JTreeFixture
import com.intellij.remoterobot.search.locators.byXpath
import com.intellij.remoterobot.utils.waitFor
import io.github.bonigarcia.wdm.WebDriverManager
import org.junit.jupiter.api.AfterAll
 import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
 import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
 import org.junit.jupiter.api.extension.ExtendWith
 import org.junit.jupiter.api.extension.ExtensionContext
 import org.junit.jupiter.api.extension.TestExecutionExceptionHandler
 import org.junit.jupiter.api.extension.BeforeTestExecutionCallback
 import org.junit.jupiter.api.extension.AfterTestExecutionCallback
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
 import java.lang.management.ManagementFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration
 import java.io.File
 import java.time.LocalDateTime
 import java.time.format.DateTimeFormatter
import java.time.Instant
import org.junit.jupiter.api.TestInfo
import org.openqa.selenium.*
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait

@ExtendWith(TestRetryHandler::class)

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BaseActionTest : ScreenRec() {
    protected lateinit var remoteRobot: RemoteRobot
    protected val metricsCollector = TestMetricsCollector()
    protected lateinit var testInfo: TestInfo

    companion object {
        private val log: Logger = LoggerFactory.getLogger(this.javaClass)

        const val PROJECT_TREE_XPATH: String = "//div[@class='ProjectViewTree']"

        const val AI_CODER_MENU_XPATH: String = "//div[contains(@class, 'ActionMenu') and contains(@text, 'AI Coder')]"
        const val MAX_RETRIES: Int = 3
        const val RETRY_DELAY_MS: Long = 1000L
       // Common test timeouts
       val SHORT_TIMEOUT = Duration.ofSeconds(10)
       val MEDIUM_TIMEOUT = Duration.ofSeconds(30)
       val LONG_TIMEOUT = Duration.ofSeconds(90)


        fun clickElement(driver: WebDriver, wait: WebDriverWait, selector: String) = runElement(
            driver, wait, selector, """
                arguments[0].scrollIntoView(true);
                arguments[0].click();
            """.trimIndent()
        )

        fun runElement(
            driver: WebDriver,
            wait: WebDriverWait,
            selector: String,
            js: String
        ): WebElement {
            while (true) {
                try {
                    return wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(selector))).apply {
                        (driver as JavascriptExecutor).executeScript(js, this)
                    }
                } catch (e: WebDriverException) {
                    if (e is TimeoutException) throw e
                    log.info("Failed to click $selector: ${e.message}")
                }
            }
        }
        fun <T>runElement(
            wait: WebDriverWait,
            selector: String,
            fn: (WebElement) -> T
        ): T {
            while (true) {
                try {
                    return wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(selector))).let { fn(it) }
                } catch (e: WebDriverException) {
                    if (e is TimeoutException) throw e
                    log.info("Failed to click $selector: ${e.message}")
                }
            }
        }
    }

    protected lateinit var driver: WebDriver
    protected var testStartTime: LocalDateTime? = null
    protected val screenshotDir = File("test-screenshots").apply { mkdirs() }
    protected fun initializeWebDriver() {
        try {
            WebDriverManager.chromedriver().setup()
            val options = ChromeOptions().apply {
                addArguments(
                    "--start-maximized",
                    "--remote-allow-origins=*",
                    "--disable-dev-shm-usage",
                    "--no-sandbox"
                )
            }
            driver = ChromeDriver(options)
            (driver as JavascriptExecutor).executeScript("document.body.style.zoom='150%'")
        } catch (e: Exception) {
            log.error("Failed to initialize WebDriver", e)
            throw RuntimeException("WebDriver initialization failed", e)
        }
    }
    @BeforeEach
    fun setUp(testInfo: TestInfo) {
        this.testInfo = testInfo
        testStartTime = LocalDateTime.now()
        log.info("Starting test at ${testStartTime}")
    }
    @AfterEach
    fun tearDownTest() {
        if (::driver.isInitialized) {
            try {
                takeScreenshot("test_end")
                driver.quit()
            } catch (e: Exception) {
                log.error("Error during test teardown", e)
            }
        }
    }
    protected fun takeScreenshot(description: String) {
        try {
            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
            val screenshotFile = File(screenshotDir, "${javaClass.simpleName}_${description}_$timestamp.png")
            (driver as JavascriptExecutor).executeScript("window.scrollTo(0, 0)")
            (driver as TakesScreenshot).getScreenshotAs(OutputType.FILE).copyTo(screenshotFile)
            log.info("Screenshot saved: ${screenshotFile.absolutePath}")
        } catch (e: Exception) {
            log.error("Failed to take screenshot", e)
        }
    }
    class MetricsCollectorExtension : BeforeTestExecutionCallback, AfterTestExecutionCallback {
        override fun beforeTestExecution(context: ExtensionContext) {
            val testInstance = context.testInstance.get() as BaseActionTest
            val testId = context.uniqueId
            testInstance.metricsCollector.getExecutionMetrics(testId).apply {
                setupTime = Duration.between(testInstance.testStartTime, Instant.now())
            }
        }
        override fun afterTestExecution(context: ExtensionContext) {
            val testInstance = context.testInstance.get() as BaseActionTest
            val testId = context.uniqueId
            testInstance.metricsCollector.getExecutionMetrics(testId).apply {
                testRunTime = Duration.between(testInstance.testStartTime, Instant.now())
                memoryUsageMB = getMemoryUsage()
                cpuUtilization = getCpuUtilization()
            }
        }
        private fun getMemoryUsage(): Long {
            val runtime = Runtime.getRuntime()
            return (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
        }
        private fun getCpuUtilization(): Double {
            val bean = ManagementFactory.getOperatingSystemMXBean()
            return if (bean is com.sun.management.OperatingSystemMXBean) {
                bean.processCpuLoad * 100
            } else 0.0
        }
    }
    protected fun retryOnFailure(maxAttempts: Int = MAX_RETRIES, action: () -> Unit) {
        var attempts = 0
        var lastException: Exception? = null
        val startTime = Instant.now()
        while (attempts < maxAttempts) {
            try {
                action()
                if (attempts > 0) {
                    metricsCollector.getExecutionMetrics(testInfo.displayName).retryCount++
                }
                return
            } catch (e: Exception) {
                lastException = e
                log.warn("Attempt ${attempts + 1} failed: ${e.message}")
                metricsCollector.getExecutionMetrics(testInfo.displayName).errorCount++
                attempts++
                if (attempts < maxAttempts) Thread.sleep(RETRY_DELAY_MS)
            }
        }
        metricsCollector.recordMetric(
            "retry_duration",
            Duration.between(startTime, Instant.now()).toMillis(),
            mapOf("operation" to action.toString())
        )
        throw lastException ?: RuntimeException("Action failed after $maxAttempts attempts")
    }
    protected fun measureOperation(name: String, action: () -> Unit) {
        val startTime = Instant.now()
        try {
            action()
        } finally {
            val duration = Duration.between(startTime, Instant.now())
            metricsCollector.recordMetric(
                "operation_duration",
                duration.toMillis(),
                mapOf("operation" to name)
            )
        }
    }

    protected fun JTreeFixture.expandAll(path: Array<String>) {
        (0 until path.size - 1).forEach { i ->
            waitFor(Duration.ofSeconds(10)) {
                try {
                    this.expand(*path.sliceArray(0..i))
                    log.info("Navigated to ${path[i]}")
                    true
                } catch (e: Exception) {
//                    log.warn("Failed to navigate to ${path[i]}: ${e.message}")
                    false
                }
            }
        }
    }

    protected fun openProjectView() {
        waitFor(SHORT_TIMEOUT) {
            try {
                remoteRobot.find(CommonContainerFixture::class.java, byXpath(PROJECT_TREE_XPATH)).click()
                log.info("Project view opened")
                true
            } catch (e: Exception) {
                log.info("Failed to open project view: ${e.message}")
                false
            }
        }
    }


    protected fun selectAICoderMenu() {
        measureOperation("selectAICoderMenu") {
            waitFor(SHORT_TIMEOUT) {
                try {
                    val aiCoderMenu = remoteRobot.find(CommonContainerFixture::class.java, byXpath(AI_CODER_MENU_XPATH))
                    aiCoderMenu.click()
                    log.info("'AI Coder' menu clicked")
                    true
                } catch (e: Exception) {
                    log.info("Failed to find or click 'AI Coder' menu: ${e.message}")
                    false
                }
            }
        }
    }
    private fun getNetworkCallCount(): Int {
        return (driver as JavascriptExecutor)
            .executeScript("return window.performance.getEntries().length")
            .toString().toInt()
    }
    private fun getFileOperationCount(): Int {
        return screenshotDir.listFiles()?.size ?: 0
    }


    @BeforeAll
    fun setup() {
        remoteRobot = RemoteRobot("http://127.0.0.1:8082")
        startUdpServer()
        System.setProperty("webdriver.chrome.driver", "/usr/bin/chromedriver")
        try {
            startScreenRecording()
        } catch (e: Exception) {
            log.error("Failed to start screen recording", e)
            throw e;
        }
    }

    @AfterAll
    fun tearDown() {
        stopUdpServer()
        if (::driver.isInitialized) {
            driver.quit()
        }
        stopScreenRecording()
        clearMessageBuffer()
    }

}