package com.simiacryptus.aicoder.demotest.action.agent

import com.intellij.remoterobot.fixtures.CommonContainerFixture
import com.intellij.remoterobot.fixtures.JCheckboxFixture
import com.intellij.remoterobot.fixtures.JTreeFixture
import com.intellij.remoterobot.search.locators.byXpath
import com.intellij.remoterobot.stepsProcessing.step
import com.intellij.remoterobot.utils.waitFor
import com.simiacryptus.aicoder.demotest.DemoTestBase
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import org.slf4j.LoggerFactory
import java.lang.Thread.sleep
import java.time.Duration

/**
 * Integration test for the Command Autofix feature of the AI Coder plugin.
 *
 * Prerequisites:
 * - IntelliJ IDEA must be running with the AI Coder plugin installed
 * - A project named "DataGnome" must be open and accessible
 * - The IDE should be in its default layout with no dialogs open
 * - Remote Robot server must be running and accessible
 *
 * Test Workflow:
 * 1. Opens the project view panel
 * 2. Navigates to the "DataGnome" directory in the project tree
 * 3. Opens the AI Coder menu
 * 4. Initiates the Auto-Fix action
 * 5. Configures Auto-Fix settings (enables auto-apply fixes)
 * 6. Interacts with the web-based Command Autofix interface
 * 7. Verifies successful build completion
 * 8. Handles potential errors with up to 5 retry attempts
 *
 * Expected Results:
 * - The Command Autofix process should complete successfully
 * - The build should remain successful after fixes are applied
 * - All UI interactions should be properly logged and narrated
 */

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CommandAutofixActionTest : DemoTestBase() {

    companion object {
        val log = LoggerFactory.getLogger(CommandAutofixActionTest::class.java)
    }

    @Test
    fun testCommandAutofixAction() = with(remoteRobot) {
        speak("This demo showcases the Command Autofix feature, which automatically identifies and fixes issues across the codebase.")
        log.info("Starting testCommandAutofixAction")
        sleep(2000)

        step("Open project view") {
            speak("Opening the project view to access the project structure.")
            openProjectView()
        }

        step("Select a directory") {
            speak("Selecting a directory to apply Command Autofix.")
            val path = arrayOf("DataGnome")
            val tree = remoteRobot.find(JTreeFixture::class.java, byXpath(PROJECT_TREE_XPATH)).apply { expandAll(path) }
            waitFor(Duration.ofSeconds(10)) { tree.rightClickPath(*path, fullMatch = false); true }
        }

        step("Select 'AI Coder' menu") {
            speak("Accessing the AI Coder menu.")
            selectAICoderMenu()
        }

        step("Click 'Auto-Fix' action") {
            speak("Selecting the 'Auto-Fix' action.")
            waitFor(Duration.ofSeconds(10)) {
                try {
          findAll(CommonContainerFixture::class.java, byXpath("//div[contains(@class, 'ActionMenuItem') and contains(@text, 'Agents')]"))
            .firstOrNull()?.click()
          sleep(1000)
                    findAll(CommonContainerFixture::class.java, byXpath("//div[contains(@class, 'ActionMenuItem') and contains(@text, 'Run ... and Fix')]"))
                        .firstOrNull()?.click()
                    log.info("'Auto-Fix' action clicked")
                    true
                } catch (e: Exception) {
                    log.warn("Failed to find 'Auto-Fix' action: ${e.message}")
                    speak("Failed to find Auto-Fix action. Retrying.")
                    false
                }
            }
        }

        step("Configure Command Autofix") {
            speak("Configuring Command Autofix settings.")
            waitFor(Duration.ofSeconds(10)) {
                val dialog = find(CommonContainerFixture::class.java, byXpath("//div[@class='MyDialog' and @title='Command Autofix Settings']"))
                if (dialog.isShowing) {

                    val autoFixCheckbox = dialog.find(JCheckboxFixture::class.java, byXpath("//div[@class='JCheckBox' and @text='Auto-apply fixes']"))
                    autoFixCheckbox.select()
                    speak("Enabled 'Auto-apply fixes' option.")

                    val okButton = dialog.find(CommonContainerFixture::class.java, byXpath("//div[@class='JButton' and @text='OK']"))
                    okButton.click()
                    log.info("Command Autofix configured and started")
                    true
                } else {
                    false
                }
            }
        }
        sleep(1000)

        step("Interact with Command Autofix interface") {
            val messages = getReceivedMessages()
            val url = messages.firstOrNull { it.startsWith("http") }
            if (url != null) {
                log.info("Retrieved URL: $url")
                speak("Command Autofix interface opened in a new window.")
                try {
                    this@CommandAutofixActionTest.driver.get(url)
                } catch (e: Exception) {
                    log.error("Failed to initialize browser", e)
                    throw e
                }

                var attempt = 1
                while (attempt <= 5) {
                    val wait = WebDriverWait(this@CommandAutofixActionTest.driver, Duration.ofSeconds(600))
                    try {
                        blockUntilDone(wait)
                        speak("Command Autofix operation completed.")
                        val codeElements = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.tagName("code")))
                        val buildSuccessful = codeElements.any { it.text.contains("BUILD SUCCESSFUL") }
                        require(buildSuccessful) { "BUILD SUCCESSFUL not found in any code element" }
                        speak("Command Autofix operation completed successfully. Build remains successful.")
                        break
                    } catch (e: Exception) {
                        attempt++
                        log.warn("Error interacting with Command Autofix interface", e)
                        blockUntilDone(wait)
                        speak("Error encountered. Retrying.")
                        (driver as JavascriptExecutor).executeScript("window.scrollTo(0, 0)")
                        val refreshButton = driver.findElement(By.xpath("//a[@class='href-link' and text()='♻']"))
                        refreshButton.click()
                        log.info("Refresh button clicked")
                        speak("Refreshing Command Autofix interface.")
                        driver.findElements(By.cssSelector(".tabs-container > .tabs > .tab-button")).get(attempt - 1).click()
                    }
                }
                this@CommandAutofixActionTest.driver.quit()
            } else {
                log.error("No URL found in UDP messages")
                speak("Error retrieving Command Autofix interface URL.")
            }
            clearMessageBuffer()
        }

        speak("Demo concluded. Command Autofix feature demonstrated.")
    }

    private fun blockUntilDone(wait: WebDriverWait) {
        wait.until {
            try {
                (driver as JavascriptExecutor).executeScript("window.scrollTo(0, document.body.scrollHeight)")
                val loadingElements = driver.findElements(By.xpath("//span[@class='sr-only' and text()='Loading...']"))
                loadingElements.all { !it.isDisplayed }
            } catch (e: Exception) {
                log.warn("Error waiting for loading elements", e)
                false
            }
        }
    }
}