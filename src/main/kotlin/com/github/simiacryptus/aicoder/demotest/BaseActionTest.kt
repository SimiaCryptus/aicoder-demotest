package com.github.simiacryptus.aicoder.demotest

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.fixtures.CommonContainerFixture
import com.intellij.remoterobot.fixtures.JTreeFixture
import com.intellij.remoterobot.search.locators.byXpath
import com.intellij.remoterobot.utils.waitFor
import io.github.bonigarcia.wdm.WebDriverManager
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BaseActionTest : ScreenRec() {
    protected lateinit var remoteRobot: RemoteRobot

    companion object {
        private val log: Logger = LoggerFactory.getLogger(this.javaClass)

        const val PROJECT_TREE_XPATH = "//div[@class='ProjectViewTree']"

        const val AI_CODER_MENU_XPATH = "//div[contains(@class, 'ActionMenu') and contains(@text, 'AI Coder')]"
    }

    protected lateinit var driver: WebDriver
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
        waitFor(Duration.ofSeconds(10)) {
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
        waitFor(Duration.ofSeconds(10)) {
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