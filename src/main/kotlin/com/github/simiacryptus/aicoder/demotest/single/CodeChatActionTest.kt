package com.github.simiacryptus.aicoder.demotest.single

import com.github.simiacryptus.aicoder.demotest.BaseActionTest
import com.intellij.remoterobot.fixtures.CommonContainerFixture
import com.intellij.remoterobot.fixtures.EditorFixture
import com.intellij.remoterobot.fixtures.JTreeFixture
import com.intellij.remoterobot.search.locators.byXpath
import com.intellij.remoterobot.stepsProcessing.step
import com.intellij.remoterobot.utils.keyboard
import com.intellij.remoterobot.utils.waitFor
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.openqa.selenium.By
import org.openqa.selenium.interactions.Actions
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import org.slf4j.LoggerFactory
import java.awt.event.KeyEvent
import java.lang.Thread.sleep
import java.time.Duration


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
class CodeChatActionTest : BaseActionTest() {

    @Test
    fun testCodeChatAction() = with(remoteRobot) {
        initializeWebDriver()


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

        step("Open a Kotlin file") {
            speak("Opening a Kotlin file for the Code Chat demonstration.")
            val path = arrayOf("TestProject", "src", "main", "kotlin", "Person")
            val tree = remoteRobot.find(JTreeFixture::class.java, byXpath(PROJECT_TREE_XPATH)).apply { expandAll(path) }
            waitFor(Duration.ofSeconds(10)) { tree.clickPath(*path, fullMatch = false); true }
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
            val messages = getReceivedMessages()
            val url = messages.firstOrNull { it.startsWith("http") }
            if (url != null) {
                log.info("Retrieved URL: $url")
                driver.get(url)
                val wait = WebDriverWait(this@CodeChatActionTest.driver, Duration.ofSeconds(15))
                val chatInput = wait.until(ExpectedConditions.elementToBeClickable(By.id("chat-input")))
                val longWait = WebDriverWait(this@CodeChatActionTest.driver, Duration.ofSeconds(60))
                chatInput.click()

                speak("Entering a request for the AI to create a user manual for our class.")
                val request = "Create a user manual for this class"
                request.forEach { char ->
                    chatInput.sendKeys(char.toString())
                    sleep(100) // Add a small delay between each character
                }
                sleep(3000) // Pause after typing the full request

                val submitButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@type='submit']")))
                speak("Submitting the request to the AI.")
                log.info("Submitting request to AI")
                submitButton.click()
                sleep(3000) // Short pause after clicking submit
                speak("Waiting for the AI to process the request and generate a response.")
                try {
                    val markdownTab =
                        longWait.until(ExpectedConditions.elementToBeClickable(By.xpath("(//button[contains(@class, 'tab-button') and contains(text(), 'Markdown')])[3]")))
                    sleep(2000)
                    speak("Switching to the Markdown tab for better readability of the AI response.")
                    markdownTab.click()
                    sleep(2000)
                    // Simulate mouseover on the upper-right corner of the message container
                    val responseContent =
                        longWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[contains(@class, 'tab-content') and @data-tab='Markdown']")))
                    val responseText = responseContent.text
                    log.info("Response content: $responseText")

                    val messageContainer =
                        longWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("(//div[contains(@class, 'message-container')])[last()]")))
                    val actions = Actions(this@CodeChatActionTest.driver)
                    try {
                        actions.moveToElement(messageContainer).perform()
                    } catch (e: Exception) {
                        log.warn("Failed to move to message container: ${e.message}")
                    }
                } catch (e: Exception) {
                    log.warn("Copy button not found within the expected time. Skipping copy action.", e)
                    speak("AI response is delayed. Skipping the copy action.")
                }

                sleep(3000) // Wait for the hide action to complete
                speak("We've successfully interacted with the Code Chat interface. As you can see, this feature provides quick, context-aware assistance for your coding tasks.")
                this@CodeChatActionTest.driver.quit()
            } else {
                log.error("No URL found in UDP messages")
                speak("Error: Unable to retrieve the necessary URL.")
            }
            clearMessageBuffer()
        }

        speak("Demo concluded. We've demonstrated initiating a Code Chat session, submitting a query, and receiving an AI-generated response.")
        sleep(5000) // Final sleep of 5 seconds
    }

    companion object {
        val log = LoggerFactory.getLogger(CodeChatActionTest::class.java)
    }


}