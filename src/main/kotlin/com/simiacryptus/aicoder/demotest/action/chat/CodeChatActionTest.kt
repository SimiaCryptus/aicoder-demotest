package com.simiacryptus.aicoder.demotest.action.chat

import com.intellij.remoterobot.fixtures.CommonContainerFixture
import com.intellij.remoterobot.fixtures.EditorFixture
import com.intellij.remoterobot.fixtures.JTreeFixture
import com.intellij.remoterobot.search.locators.byXpath
import com.intellij.remoterobot.stepsProcessing.step
import com.intellij.remoterobot.utils.keyboard
import com.intellij.remoterobot.utils.waitFor
import com.simiacryptus.aicoder.demotest.DemoTestBase
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.openqa.selenium.By
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import org.slf4j.LoggerFactory
import java.awt.event.KeyEvent
import java.lang.Thread.sleep
import java.time.Duration
import kotlin.io.path.name


/**
 * Tests the Code Chat functionality of the AI Coder plugin.
 *
 * Prerequisites:
 * - IntelliJ IDEA must be running with the AI Coder plugin installed
 * - A test project must be open with the following structure:
 *   - TestProject/
 *     - src/
 *       - main/
 *         - kotlin/
 *           - Person.kt (containing a basic Kotlin class)
 * - The IDE should be in its default layout with no other dialogs open
 * - The AI Coder plugin should be properly configured with valid API credentials
 *
 * Test Behavior:
 * 1. Opens the Project view panel if not already open
 * 2. Navigates to and opens the Person.kt file
 * 3. Selects all code in the editor
 * 4. Opens the context menu via right-click
 * 5. Navigates through the AI Coder menu to select Code Chat
 * 6. Waits for the chat interface to open in a browser
 * 7. Types a request for creating a user manual
 * 8. Submits the request and waits for AI response
 * 9. Switches to Markdown view of the response
 * 10. Verifies the response and closes the browser
 *
 * Note: This test includes voice feedback for demonstration purposes
 * and includes appropriate waits between actions to ensure stability.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CodeChatActionTest : DemoTestBase() {
    override fun getTemplateProjectPath(): String {
        return "demo_projects/TestProject"
    }

    @Test
    fun testCodeChatAction() = with(remoteRobot) {
        speak("Welcome to the AI Coder demo. We'll explore the Code Chat feature, which enables AI interaction for code-related queries and assistance.")
        log.info("Starting testCodeChatAction")
        sleep(2000)

        step("Open project view") {
            openProjectView()
            log.info("Project view opened")
            try {
                speak("Opening the project view to access files.")
            } catch (e: Exception) {
                log.warn("Failed to provide audio feedback: ${e.message}")
            }

            sleep(2000)
        }

        val projectName = testProjectDir.fileName.name
        step("Open a Kotlin file") {
            speak("Opening a Kotlin file for the Code Chat demonstration.")
            val path = arrayOf(projectName, "src", "main", "kotlin", "Person")
            val tree = remoteRobot.find(JTreeFixture::class.java, byXpath(PROJECT_TREE_XPATH)).apply { expandAll(path) }
            waitFor(Duration.ofSeconds(10)) { tree.doubleClickPath(*path, fullMatch = false); true }
            log.info("Kotlin file opened")
            sleep(2000)
        }

        step("Select code") {
            speak("Selecting code to provide context for the AI.")
            val editor = find(EditorFixture::class.java, byXpath("//div[@class='EditorComponentImpl']"))
            editor.click()
            keyboard {
                pressing(KeyEvent.VK_CONTROL) {
                    key(KeyEvent.VK_A) // Select all
                }
            }
            log.info("Code selected")
            sleep(2000)
        }

        step("Open context menu") {
            speak("Opening the context menu to access AI Coder features.")
            val editor = find(EditorFixture::class.java, byXpath("//div[@class='EditorComponentImpl']"))
            editor.rightClick()
            log.info("Context menu opened via right-click")
            sleep(2000)
        }

        step("Select 'AI Coder' menu") {
            speak("Selecting the 'AI Coder' option from the context menu.")
            selectAICoderMenu()
            sleep(2000)
        }

        step("Click 'Code Chat' action") {
            speak("Initiating the 'Code Chat' feature for an interactive dialogue with AI about our code.")
            waitFor(Duration.ofSeconds(15)) {
                try {
                    findAll(CommonContainerFixture::class.java, byXpath("//div[contains(@class, 'ActionMenuItem') and contains(@text, 'AI Editor')]"))
                        .firstOrNull()?.click()
                    sleep(1000)
                    findAll(CommonContainerFixture::class.java, byXpath("//div[contains(@class, 'ActionMenuItem') and contains(@text, 'Code Chat')]"))
                        .firstOrNull()?.click()
                    log.info("'Code Chat' action clicked")
                    true
                } catch (e: Exception) {
                    log.warn("Failed to find 'Code Chat' action: ${e.message}")
                    false
                }
            }
            sleep(2000)
        }

        step("Get URL from UDP messages") {
            var url: String? = null
            log.debug("Starting Code Chat interface interaction")
            waitFor(Duration.ofSeconds(90)) {
                val messages = getReceivedMessages()
                url = messages.firstOrNull { it.startsWith("http") } ?: ""
                log.debug("Searching for URL in messages. Found: ${url?.take(50) ?: "none"}")
                url?.isNotEmpty() ?: false
            }

            if (url != null) {
                log.info("Retrieved Code Chat interface URL: $url")
                speak("Code Chat web interface opened.")
                log.debug("Initializing web driver")
                driver.get(url)
                log.debug("Setting up WebDriverWait with 90 second timeout")
                val wait = WebDriverWait(driver, Duration.ofSeconds(90))

                try {
                    // Get chat input element
                    val chatInput = wait.until(ExpectedConditions.elementToBeClickable(By.id("chat-input")))
                    log.info("Chat interface loaded successfully")
                    speak("Interface loaded. Submitting request.")
                    sleep(1000)

                    // Submit request
                    log.debug("Submitting request to chat interface")
                    chatInput.click()
                    speak("Entering a request for the AI to create a user manual for our class.")
                    val request = "Create a user manual for this class"
                    chatInput.sendKeys(request)
                    sleep(1000)

                    clickElement(driver, wait, "//button[@type='submit']")
                    log.info("Request submitted successfully")
                    speak("Request submitted. Waiting for AI response.")
                    sleep(2000)
                    // Switch to Markdown view
                    log.debug("Switching to Markdown view")
                    speak("Switching to Markdown view for better readability.")
                    clickElement(driver, wait, "(//button[contains(@class, 'tab-button') and contains(text(), 'Markdown')])[3]")
                    // Get response content
                    val responseContent = wait.until(
                        ExpectedConditions.presenceOfElementLocated(
                            By.xpath("//div[contains(@class, 'tab-content') and @data-tab='Markdown']")
                        )
                    )
                    log.info("Response received: ${responseContent.text.take(200)}...")
                    speak("AI response received and displayed.")
                    sleep(3000)
                    log.info("Code Chat interaction completed successfully")
                    speak("We've successfully demonstrated the Code Chat feature.")
                } catch (e: Exception) {
                    log.error("Error during Code Chat interaction: ${e.message}", e)
                    speak("Encountered an error during Code Chat interaction. Please check the logs for details.")
                } finally {
                    log.debug("Cleaning up web driver resources")
                    driver.quit()
                    log.info("Web driver cleanup completed")
                }

            } else {
                log.error("Failed to retrieve Code Chat interface URL from UDP messages")
                speak("Error: Unable to retrieve the necessary URL.")
            }
            log.debug("Clearing message buffer")
            clearMessageBuffer()
        }

        speak("Demo concluded. We've demonstrated initiating a Code Chat session, submitting a query, and receiving an AI-generated response.")
        sleep(5000) // Final sleep of 5 seconds
    }

    companion object {
        val log = LoggerFactory.getLogger(CodeChatActionTest::class.java)
    }


}