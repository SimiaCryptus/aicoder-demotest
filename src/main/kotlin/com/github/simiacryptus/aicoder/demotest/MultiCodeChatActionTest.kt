package com.github.simiacryptus.aicoder.demotest

import com.intellij.remoterobot.fixtures.CommonContainerFixture
import com.intellij.remoterobot.fixtures.JTreeFixture
import com.intellij.remoterobot.search.locators.byXpath
import com.intellij.remoterobot.stepsProcessing.step
import com.intellij.remoterobot.utils.waitFor
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.slf4j.LoggerFactory
import java.time.Duration
import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.interactions.Actions
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MultiCodeChatActionTest : BaseActionTest() {


    companion object {
        val log = LoggerFactory.getLogger(MultiCodeChatActionTest::class.java)
    }
    @Test
    fun testMultiCodeChatAction() = with(remoteRobot) {
    TestUtil.speak("This demo showcases the Multi-Code Chat feature, which enables simultaneous analysis of multiple code files.")
        log.info("Starting testMultiCodeChatAction")
        Thread.sleep(2000)

        step("Open project view") {
        TestUtil.speak("Opening the project view to access code files.")
            find(CommonContainerFixture::class.java, byXpath("//div[@class='ProjectViewTree']")).click()
            log.info("Project view opened")
            Thread.sleep(2000)
        }

        step("Select multiple Kotlin files") {
        TestUtil.speak("Selecting a Kotlin file in the project structure.")
            val projectTree = find(JTreeFixture::class.java, byXpath("//div[@class='ProjectViewTree']"))
            projectTree.clickPath(*arrayOf("TestProject", "src", "main", "kotlin"), fullMatch = false)
            projectTree.clickPath(*arrayOf("TestProject", "src", "main", "kotlin", "Person"), fullMatch = false)
            log.info("Kotlin file selected")
            Thread.sleep(3000)
        }

        step("Open context menu") {
        TestUtil.speak("Opening the context menu to access AI Coder features.")
            val projectTree = find(JTreeFixture::class.java, byXpath("//div[@class='ProjectViewTree']"))
            projectTree.rightClick()
            log.info("Context menu opened via right-click")
            Thread.sleep(2000)
        }

        step("Select 'AI Coder' menu") {
        TestUtil.speak("Selecting the AI Coder option from the context menu.")
            waitFor(Duration.ofSeconds(10)) {
                try {
                    val aiCoderMenu = find(CommonContainerFixture::class.java, byXpath("//div[contains(@class, 'ActionMenu') and contains(@text, 'AI Coder')]"))
                    aiCoderMenu.click()
                    log.info("'AI Coder' menu clicked")
                    true
                } catch (e: Exception) {
                    log.warn("Failed to find or click 'AI Coder' menu: ${e.message}")
                    false
                }
            }
            Thread.sleep(2000)
        }

        step("Click 'Multi-Code Chat' action") {
        TestUtil.speak("Initiating the Multi-Code Chat action.")
            waitFor(Duration.ofSeconds(10)) {
                try {
                    findAll(CommonContainerFixture::class.java, byXpath("//div[contains(@class, 'ActionMenuItem') and contains(@text, 'Code Chat')]"))
                        .firstOrNull()?.click()
                    log.info("'Multi-Code Chat' action clicked")
                    true
                } catch (e: Exception) {
                    log.warn("Failed to find 'Multi-Code Chat' action: ${e.message}")
                    false
                }
            }
            Thread.sleep(3000)
        }

        step("Get URL from UDP messages") {
            val messages = TestUtil.getReceivedMessages()
            val url = messages.firstOrNull { it.startsWith("http") }
            if (url != null) {
                log.info("Retrieved URL: $url")
                TestUtil.speak("Launching the Multi-Code Chat interface in a new browser window.")
                val options = ChromeOptions()
                options.addArguments("--start-fullscreen")
                driver = ChromeDriver(options)
                (driver as JavascriptExecutor).executeScript("document.body.style.zoom='150%'")
                driver.get(url)
                val wait = WebDriverWait(this@MultiCodeChatActionTest.driver, Duration.ofSeconds(10))
                val chatInput = wait.until<WebElement>(ExpectedConditions.elementToBeClickable(By.id("chat-input")))
                chatInput.click()
                Thread.sleep(2000)
                TestUtil.speak("Entering a request to analyze the selected code.")
                val request = "Analyze this class"
                request.forEach { char ->
                    chatInput.sendKeys(char.toString())
                    Thread.sleep(100) // Add a small delay between each character
                }
                Thread.sleep(3000) // Pause after typing the full request
                val submitButton = wait.until<WebElement>(ExpectedConditions.elementToBeClickable(By.xpath("//button[@type='submit']")))
                TestUtil.speak("Submitting the request for AI analysis.")
                log.info("Submitting request to AI")
                submitButton.click()
                TestUtil.speak("AI is analyzing the code. This process typically takes a few seconds.")
                // Wait for the response to be generated with a longer timeout
                val longWait = WebDriverWait(this@MultiCodeChatActionTest.driver, Duration.ofSeconds(60))
                try {
                    val markdownTab = longWait.until<WebElement>(ExpectedConditions.elementToBeClickable(By.xpath("(//button[contains(@class, 'tab-button') and contains(text(), 'Markdown')])[3]")))
                    Thread.sleep(2000)
                    TestUtil.speak("Viewing the AI's response in Markdown format for better readability.")
                    markdownTab.click()
                    Thread.sleep(3000)
                    // Simulate mouseover on the upper-right corner of the message container
                    val messageContainer =
                        longWait.until<WebElement>(ExpectedConditions.presenceOfElementLocated(By.xpath("(//div[contains(@class, 'message-container')])[last()]")))
                    val actions = Actions(this@MultiCodeChatActionTest.driver)
                    try {
                        actions.moveToElement(messageContainer).perform()
                        TestUtil.speak("Demonstrating message options for copying, editing, or performing other actions on the AI's response.")
                        Thread.sleep(3000)
                    } catch (e: Exception) {
                        log.warn("Failed to move to message container: ${e.message}")
                    }
                } catch (e: Exception) {
                    log.warn("Copy button not found within the expected time. Skipping copy action.", e)
                    TestUtil.speak("AI response is delayed. In a real scenario, consider refreshing or checking network connection.")
                    Thread.sleep(3000)
                }

                Thread.sleep(3000) // Wait for the hide action to complete
                TestUtil.speak("Demonstration of Multi-Code Chat interface complete.")
                this@MultiCodeChatActionTest.driver.quit()
            } else {
                log.error("No URL found in UDP messages")
                TestUtil.speak("Error retrieving Multi-Code Chat URL. In a real scenario, retry or contact support.")
                Thread.sleep(3000)
            }
            TestUtil.clearMessageBuffer()
        }

        TestUtil.speak("Demo concluded. Multi-Code Chat enables efficient code analysis and review.")
    }
}