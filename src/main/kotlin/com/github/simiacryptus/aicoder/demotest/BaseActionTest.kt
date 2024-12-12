package com.github.simiacryptus.aicoder.demotest

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.fixtures.CommonContainerFixture
import com.intellij.remoterobot.fixtures.JTreeFixture
import com.intellij.remoterobot.search.locators.byXpath
import com.intellij.remoterobot.utils.waitFor
import io.github.bonigarcia.wdm.WebDriverManager
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.openqa.selenium.*
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@ExtendWith(TestRetryHandler::class)

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BaseActionTest : ScreenRec() {
  protected lateinit var remoteRobot: RemoteRobot
  protected val robot: java.awt.Robot = java.awt.Robot()

  companion object {
    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    const val PROJECT_TREE_XPATH: String = "//div[@class='ProjectViewTree']"
    const val AI_CODER_MENU_XPATH: String = "//div[contains(@class, 'ActionMenu') and contains(@text, 'AI Coder')]"
    val SHORT_TIMEOUT = Duration.ofSeconds(10)

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

    fun <T> runElement(
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


  protected fun selectAICoderMenu(): CommonContainerFixture {
    lateinit var aiCoderMenu: CommonContainerFixture
    waitFor(SHORT_TIMEOUT) {
      try {
        aiCoderMenu = remoteRobot.find(CommonContainerFixture::class.java, byXpath(AI_CODER_MENU_XPATH))
        aiCoderMenu.click()
        log.info("'AI Coder' menu clicked")
        true
      } catch (e: Exception) {
        log.info("Failed to find or click 'AI Coder' menu: ${e.message}")
        false
      }
    }
    return aiCoderMenu
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