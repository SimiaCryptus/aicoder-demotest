package com.simiacryptus.aicoder.demotest.action.chat

import com.intellij.remoterobot.fixtures.CommonContainerFixture
import com.intellij.remoterobot.fixtures.JTreeFixture
import com.intellij.remoterobot.search.locators.byXpath
import com.intellij.remoterobot.stepsProcessing.step
import com.intellij.remoterobot.utils.waitFor
import com.simiacryptus.aicoder.demotest.DemoTestBase
import com.simiacryptus.aicoder.demotest.SplashScreenConfig
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.openqa.selenium.By
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import org.slf4j.LoggerFactory
import java.lang.Thread.sleep
import java.time.Duration
import kotlin.io.path.name

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LargeOutputCodeChatActionTest : DemoTestBase(
    splashScreenConfig = SplashScreenConfig(
        titleText = "Large Output Code Chat Demo",
    )
) {
    companion object {
        val log = LoggerFactory.getLogger(LargeOutputCodeChatActionTest::class.java)
    }

    @Test
    fun testLargeOutputCodeChatAction() = with(remoteRobot) {
        tts("Welcome to the Large Output Code Chat demonstration. This feature provides an enhanced AI coding assistant with structured responses.")?.play(
            2000
        )

        step("Open project view") {
            openProjectView()
            MultiCodeChatActionTest.log.info("Project view opened")
            try {
                tts("Let's start by accessing our project files. The Multi-Code Chat feature works best when analyzing related code files together.")?.play(
                    2000
                )
            } catch (e: Exception) {
                MultiCodeChatActionTest.log.warn("Failed to provide audio feedback: ${e.message}")
            }
        }
        val projectName = testProjectDir.fileName.name

        step("Select multiple Kotlin files") {
            try {
                tts("We'll select our main Kotlin file. In practice, you can select multiple files to analyze their relationships and interactions.")?.play()
                val path = arrayOf(projectName, "src", "main", "kotlin", "Main.kt")
                val tree =
                    remoteRobot.find(JTreeFixture::class.java, byXpath(PROJECT_TREE_XPATH)).apply { expandAll(path) }
                waitFor(Duration.ofSeconds(10)) { tree.rightClickPath(*path, fullMatch = false); true }
                MultiCodeChatActionTest.log.info("Kotlin file selected")
            } catch (e: Exception) {
                MultiCodeChatActionTest.log.error("Failed to select Kotlin files", e)
                tts("If you encounter issues selecting files, ensure they are accessible and you have proper permissions.")?.play()
            }
            sleep(2000)
        }

        step("Select 'AI Coder' menu") {
            tts("Now we'll access the AI Coder menu. This contains all our AI-powered development tools.")?.play(2000)
            selectAICoderMenu()
        }

        step("Click 'Large Output Code Chat' action") {
            tts("Let's launch the Multi-Code Chat interface. This will open a dedicated chat window for code analysis and discussion.")?.play()
            waitFor(Duration.ofSeconds(15)) {
                try {
                    findAll(
                        CommonContainerFixture::class.java,
                        byXpath("//div[contains(@class, 'ActionMenuItem') and contains(@text, 'Large Output Code Chat')]")
                    )
                        .firstOrNull()?.click()
                    MultiCodeChatActionTest.log.info("'Multi-Code Chat' action clicked")
                    true
                } catch (e: Exception) {
                    MultiCodeChatActionTest.log.warn("Failed to find 'Multi-Code Chat' action: ${e.message}")
                    false
                }
            }
            sleep(2000)
        }

        step("Navigate to Large Output Code Chat action") {
            tts("Under the Generate submenu, we'll find the Create Image action. This tool can generate various types of diagrams including UML, flowcharts, and architectural visualizations.")?.play()
            waitFor(Duration.ofSeconds(15)) {
                try {
                    // Find and hover over Generate menu
                    findAll(CommonContainerFixture::class.java, byXpath("//div[@text='⚡ Generate']"))
                        .firstOrNull()?.moveMouse()
                    sleep(1000)
                    // Click Create Image option
                    findAll(
                        CommonContainerFixture::class.java,
                        byXpath("//div[contains(@class, 'ActionMenuItem') and contains(@text, 'Large Output Code Chat')]")
                    )
                        .firstOrNull()?.click()
                    log.info("Large Output Code action clicked")
                    true
                } catch (e: Exception) {
                    log.warn("Failed to find Large Output Code Chat action: ${e.message}")
                    false
                }
            }
            sleep(2000)
        }

        step("Interact with chat interface") {
            var url: String? = null
            log.debug("Starting chat interface interaction")
            waitFor(Duration.ofSeconds(90)) {
                val messages = getReceivedMessages()
                url = messages.firstOrNull { it.startsWith("http") } ?: ""
                log.debug("Searching for URL in messages. Found: ${url?.take(50) ?: "none"}")
                url?.isNotEmpty() ?: false
            }

            if (url != null) {
                log.info("Retrieved chat interface URL: $url")
                tts("The chat interface has launched successfully.")?.play()
                driver.get(url)
                val wait = WebDriverWait(driver, Duration.ofSeconds(90))

                try {
                    val chatInput = wait.until(ExpectedConditions.elementToBeClickable(By.id("chat-input")))
                    log.info("Chat interface loaded successfully")
                    tts("Let's ask the AI about advanced Kotlin features.")?.play(1000)

                    log.debug("Submitting question to chat interface")
                    chatInput.click()
                    tts("Watch as we type our question.")?.play(1000)
                    chatInput.sendKeys("Can you explain advanced Kotlin features?")

                    wait.until(ExpectedConditions.elementToBeClickable(By.id("send-message-button"))).click()
                    log.info("Question submitted successfully")
                    tts("The question is submitted, and the AI is analyzing it.")?.play(2000)

                    // Wait for response
                    wait.until(ExpectedConditions.presenceOfElementLocated(By.className("response-message")))
                    tts("The AI has provided a detailed response.")?.play(3000)

                    log.info("Chat interaction completed successfully")
                    tts("We've successfully demonstrated the Large Output Code Chat feature.")?.play()
                } catch (e: Exception) {
                    log.error("Error during chat interaction: ${e.message}", e)
                    tts("Encountered an error during chat interaction. Please check the logs for details.")?.play()
                } finally {
                    log.debug("Cleaning up web driver resources")
                    driver.quit()
                    log.info("Web driver cleanup completed")
                }
            } else {
                log.error("Failed to retrieve chat interface URL from UDP messages")
                tts("Error: Unable to retrieve the necessary URL.")?.play()
            }
            log.debug("Clearing message buffer")
            clearMessageBuffer()
        }

        tts("This concludes our demonstration of the Large Output Code Chat. It provides structured responses for complex programming questions.")?.play(
            5000
        )
        return@with
    }
}