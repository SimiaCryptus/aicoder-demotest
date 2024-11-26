package com.github.simiacryptus.aicoder.demotest

import com.intellij.remoterobot.fixtures.CommonContainerFixture
import com.intellij.remoterobot.fixtures.JCheckboxFixture
import com.intellij.remoterobot.fixtures.JTreeFixture
import com.intellij.remoterobot.search.locators.byXpath
import com.intellij.remoterobot.stepsProcessing.step
import com.intellij.remoterobot.utils.keyboard
import com.intellij.remoterobot.utils.waitFor
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import org.slf4j.LoggerFactory
import java.time.Duration

/**
 * Test class for the Auto Plan Chat feature of AI Coder plugin.
 *
 * Prerequisites:
 * - IntelliJ IDEA must be running with the AI Coder plugin installed
 * - A project must be open with a standard structure
 * - The IDE should be in its default layout with no dialogs open
 *
 * Test Flow:
 * 1. Opens the Project View if not already visible
 * 2. Navigates to the source directory in the project tree
 * 3. Opens the AI Coder context menu
 * 4. Launches the Auto-Plan feature
 * 5. Interacts with the Auto-Plan web interface to:
 *    - Submit a task description
 *    - Monitor execution progress
 *    - View results
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AutoPlanActionTest : BaseActionTest() {

    companion object {
        private val log = LoggerFactory.getLogger(AutoPlanActionTest::class.java)
        private const val AUTO_PLAN_XPATH = "//div[contains(@class, 'ActionMenuItem') and contains(@text, 'Auto-Plan')]"
    }

    @Test
    fun testAutoPlanAction() = with(remoteRobot) {
        speak("Welcome to the AI Coder Auto-Plan demonstration. This feature provides automated planning and execution of coding tasks.")
        log.info("Starting testAutoPlanAction")
        Thread.sleep(3000)

        step("Open project view") {
            speak("Opening the project view to access the file structure.")
            openProjectView()
            Thread.sleep(2000)
        }

        step("Select source directory") {
            speak("Navigating to the source directory.")
            val path = arrayOf("src", "main", "kotlin")
            val tree = find(JTreeFixture::class.java, byXpath(PROJECT_TREE_XPATH)).apply { expandAll(path) }
            waitFor(Duration.ofSeconds(10)) { tree.rightClickPath(*path, fullMatch = false); true }
            log.info("Source directory selected")
            Thread.sleep(3000)
        }

        step("Open AI Coder menu") {
            speak("Opening the AI Coder menu.")
            selectAICoderMenu()
            speak("AI Coder menu opened.")
            Thread.sleep(3000)
        }

        step("Select Auto-Plan action") {
            speak("Selecting the Auto-Plan feature.")
            waitFor(Duration.ofSeconds(10)) {
                try {
                    findAll(CommonContainerFixture::class.java, byXpath(AUTO_PLAN_XPATH))
                        .firstOrNull()?.click()
                    log.info("Auto-Plan action clicked")
                    speak("Auto-Plan feature initiated.")
                    true
                } catch (e: Exception) {
                    log.warn("Failed to find Auto-Plan action: ${e.message}")
                    speak("Failed to find Auto-Plan action. Retrying...")
                    false
                }
            }
            Thread.sleep(3000)
        }

        step("Configure Task Runner") {
            speak("Configuring Task Runner settings.")
            waitFor(Duration.ofSeconds(10)) {
                val dialog = find(CommonContainerFixture::class.java, byXpath("//div[@class='MyDialog' and @title='Configure Plan Ahead Action']"))
                if (dialog.isShowing) {
                    dialog.find(JCheckboxFixture::class.java, byXpath("//div[@class='JCheckBox' and @text='Auto-apply fixes']")).apply {
                        if (!isSelected()) {
                            click()
                            log.info("Auto-apply fixes checkbox selected")
                            speak("Enabled auto-apply fixes option.")
                        }
                    }
                    dialog.find(JCheckboxFixture::class.java, byXpath("//div[@class='JCheckBox' and @text='Allow blocking']")).apply {
                        if (!isSelected()) {
                            click()
                            log.info("Enable Blocking checkbox selected")
                            speak("Enabled blocking mode.")
                        }
                    }
                    Thread.sleep(3000)

                    val okButton = dialog.find(CommonContainerFixture::class.java, byXpath("//div[@class='JButton' and @text='OK']"))
                    okButton.click()
                    log.info("Task Runner configured and started")
                    speak("Task Runner configured and started.")
                    true
                } else {
                    false
                }
            }
        }

        step("Interact with Auto-Plan interface") {
            var url: String? = null
            waitFor(Duration.ofSeconds(90)) {
                val messages = getReceivedMessages()
                url = messages.firstOrNull { it.startsWith("http") } ?: ""
                url?.isNotEmpty() ?: false
            }

            if (url != null) {
                log.info("Retrieved URL: $url")
                speak("Auto-Plan web interface opened.")
                initializeWebDriver()
                driver.get(url)

                val wait = WebDriverWait(driver, Duration.ofSeconds(90))
                val chatInput = wait.until(ExpectedConditions.elementToBeClickable(By.id("chat-input")))
                speak("Interface loaded. Submitting task description.")
                Thread.sleep(3000)

                try {
                    // Submit task description
                    chatInput.sendKeys("Create a new utility class for string manipulation with methods for common operations")
                    keyboard { enter() }
                    speak("Task description submitted.")
                    Thread.sleep(5000)

                    // Monitor execution progress
                    speak("Auto-Plan is analyzing the task and creating an execution plan.")
                    Thread.sleep(8000)

                    // Wait for and monitor thinking status updates
                    wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[contains(text(), 'Thinking Status')]")))
                    speak("AI is now processing the task and breaking it down into manageable steps.")
                    Thread.sleep(10000)

                    // Monitor task execution
                    val taskExecutions = driver.findElements(By.xpath("//div[contains(text(), 'Task Execution')]"))
                    if (taskExecutions.isNotEmpty()) {
                        speak("Tasks are being executed automatically.")
                        // Scroll to view task executions
                        (driver as JavascriptExecutor).executeScript("arguments[0].scrollIntoView(true);", taskExecutions.last())
                    }
                    Thread.sleep(15000)

                    speak("Auto-Plan has completed the task execution. You can review the results in the interface.")
                } catch (e: Exception) {
                    log.error("Error during Auto-Plan interaction", e)
                    speak("Encountered an error during Auto-Plan execution. Please check the logs for details.")
                } finally {
                    Thread.sleep(5000)
                    driver.quit()
                }
            } else {
                log.error("No URL found in UDP messages")
                speak("Failed to retrieve Auto-Plan interface URL.")
            }
            clearMessageBuffer()
        }

        speak("Auto-Plan demonstration completed. This feature automates the planning and execution of coding tasks, improving development efficiency.")
        Thread.sleep(5000)
    }
}