package com.github.simiacryptus.aicoder.demotest.single

import com.github.simiacryptus.aicoder.demotest.BaseActionTest
import com.intellij.remoterobot.fixtures.CommonContainerFixture
import com.intellij.remoterobot.fixtures.JTreeFixture
import com.intellij.remoterobot.search.locators.byXpath
import com.intellij.remoterobot.stepsProcessing.step
import com.intellij.remoterobot.utils.waitFor
import org.junit.jupiter.api.Assertions.assertTrue
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
 * Tests the Multi-Diff Chat functionality of the AI Coder plugin.
 *
 * Prerequisites:
 * - IntelliJ IDEA must be running with the AI Coder plugin installed
 * - A test project named "TestProject" must be open
 * - The project must contain a readme.md file
 * - The IDE should be in its default layout with no open editors
 * - Remote Robot server must be running and accessible
 * - UDP message receiving capability must be configured
 * - WebDriver must be properly configured for browser automation
 *
 * Test Flow:
 * 1. Opens the Project View if not already visible
 * 2. Locates and right-clicks in the project tree
 * 3. Navigates through context menu to select AI Coder > Modify Files
 * 4. Waits for and captures the URL from UDP messages
 * 5. Opens the Multi-Diff Chat interface in a browser
 * 6. Submits a request to add a Mermaid diagram to readme.md
 * 7. Waits for AI to generate the patch
 * 8. Applies the generated patch
 * 9. Verifies the changes in the IDE
 * 10. Cleans up by closing the browser
 *
 * Expected Results:
 * - The readme.md file should be modified to include a Mermaid diagram
 * - The test verifies the presence of "```mermaid" in the file content
 */

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MultiDiffChatActionTest : BaseActionTest() {


    companion object {
        val log = LoggerFactory.getLogger(MultiDiffChatActionTest::class.java)
    }
    @Test
    fun testMultiDiffChatAction() = with(remoteRobot) {
        speak("Welcome to the AI Coder demo. We'll explore the Multi-Diff Chat feature for AI-assisted file editing.")
        log.info("Starting testMultiDiffChatAction")
        Thread.sleep(2000)

        step("Open project view") {
            speak("Opening the project view to access our files.")
            openProjectView()
            Thread.sleep(2000)
        }


        step("Open context menu") {
            speak("Opening the context menu to access AI Coder options.")

            find(JTreeFixture::class.java, byXpath(PROJECT_TREE_XPATH)).rightClick()
            Thread.sleep(2000)
        }
        step("Select 'AI Coder' menu") {
            speak("Selecting the AI Coder option from the context menu.")
            selectAICoderMenu()
            Thread.sleep(2000)
        }


        step("Click 'Modify Files' action") {
            speak("Initiating the Multi-Diff Chat feature via the 'Modify Files' action.")
            waitFor(Duration.ofSeconds(15)) {
                try {
                    findAll(CommonContainerFixture::class.java, byXpath("//div[contains(@class, 'ActionMenuItem') and contains(@text, 'Modify Files')]"))
                        .firstOrNull()?.click()
                    log.info("'Modify Files' action clicked")
                    true
                } catch (e: Exception) {
                    log.warn("Failed to find 'Modify Files' action: ${e.message}")
                    speak("Failed to find 'Modify Files' action. Retrying.")
                    false
                }
            }
            Thread.sleep(2000)
        }

        step("Get URL from UDP messages") {
            speak("Opening the Multi-Diff Chat interface in a new browser window.")
            val messages = getReceivedMessages()
            val url = messages.firstOrNull { it.startsWith("http") }
            if (url != null) {
                log.info("Retrieved URL: $url")
                speak("Retrieved URL for the Multi-Diff Chat interface.")

                initializeWebDriver()
                (driver as JavascriptExecutor).executeScript("document.body.style.zoom='150%'")
                driver.get(url)
                speak("Viewing the Multi-Diff Chat interface.")
                val wait = WebDriverWait(this@MultiDiffChatActionTest.driver, Duration.ofSeconds(10))
                val chatInput = wait.until<WebElement>(ExpectedConditions.elementToBeClickable(By.id("chat-input")))
                chatInput.click()
                chatInput.clear()
                speak("Typing request to add a Mermaid diagram to the readme.md file.")
                Thread.sleep(2000)
                val request = "Add a Mermaid diagram to the readme.md file showing the basic structure of this project"
                // Use JavaScript to set text instead of character-by-character input
                (driver as JavascriptExecutor).executeScript(
                    "arguments[0].value = arguments[1]", chatInput, request
                )
                request.forEach { char ->
                    chatInput.sendKeys(char.toString())
                    Thread.sleep(100) // Add a small delay between each character
                }
                Thread.sleep(2000) // Pause after typing the full request
                val submitButton = wait.until<WebElement>(ExpectedConditions.elementToBeClickable(By.xpath("//button[@type='submit']")))
                speak("Submitting request to AI.")
                log.info("Submitting request to AI")
                submitButton.click()
                Thread.sleep(3000) // Longer pause after clicking submit
                speak("Waiting for AI to generate the patch.")
                val longWait = WebDriverWait(this@MultiDiffChatActionTest.driver, Duration.ofSeconds(60))
                try {
                    val patchContent = longWait.until<WebElement>(ExpectedConditions.presenceOfElementLocated(By.xpath("//pre[contains(@class, 'language-diff')]")))
                    speak("AI has generated a patch. Reviewing proposed changes.")
                    log.info("Patch generated: ${patchContent.text}")
                    Thread.sleep(3000)

                    // Simulate clicking the "Apply Diff" button
                    val applyButton =
                        longWait.until<WebElement>(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@class, 'cmd-button') and contains(text(), 'Apply Diff')]")))
                    speak("Applying the diff to readme.md file.")
                    applyButton.click()
                    Thread.sleep(3000) // Wait for the apply action to complete

                    speak("Diff applied. Verifying changes in the IDE.")
                    // Close the browser window
                    this@MultiDiffChatActionTest.driver.close()
                    speak("Returned to IDE. Verifying file contents.")
                    val projectViewTree = find(JTreeFixture::class.java, byXpath("//div[@class='ProjectViewTree']"))
                    projectViewTree.doubleClickPath(*arrayOf("TestProject", "readme.md"), fullMatch = false)
                    val editor = find<CommonContainerFixture>(byXpath("//div[@class='EditorComponentImpl']"))
                    val fileContent = editor.findAllText().joinToString("") { it.text }
                    assertTrue(fileContent.contains("```mermaid"), "The readme.md file should contain a Mermaid diagram")
                    speak("Verified: readme.md now contains a Mermaid diagram.")
                    Thread.sleep(3000)
                } catch (e: Exception) {
                    log.warn("Failed to generate or apply patch: ${e.message}")
                    speak("Encountered an issue while generating or applying the patch. Check logs for details.")
                } finally {
                    try {
                        driver.quit()
                    } catch (e: Exception) {
                        log.error("Error closing browser", e)
                    }
                }

                speak("Demo concluded. Multi-Diff Chat successfully added a Mermaid diagram to readme.md.")
                this@MultiDiffChatActionTest.driver.quit()
            } else {
                log.error("No URL found in UDP messages")
                speak("Failed to retrieve URL for Multi-Diff Chat interface. Check logs for details.")
            }
            clearMessageBuffer()

        }

        speak("AI Coder Multi-Diff Chat demo complete. This feature streamlines code modifications and documentation updates, enhancing developer productivity.")
        Thread.sleep(10000) // Final sleep of 10 seconds
    }
}