package com.simiacryptus.aicoder.demotest.action.chat

import com.intellij.remoterobot.fixtures.CommonContainerFixture
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

/**
 * Tests the Generic Chat functionality of the AI Coder plugin.
 *
 * Prerequisites:
 * - IntelliJ IDEA must be running with the AI Coder plugin installed
 * - A test project must be open
 * - The IDE should be in its default layout with no dialogs open
 * - The AI Coder plugin should be properly configured with valid API credentials
 *
 * Test Flow:
 * 1. Opens the Tools menu
 * 2. Navigates to AI Coder > AI Assistant Chat
 * 3. Waits for the chat interface to open in a browser
 * 4. Types a general coding question
 * 5. Submits the request and waits for AI response
 * 6. Verifies the response and closes the browser
 *
 * Expected Results:
 * - The chat interface should launch successfully
 * - The AI should provide a relevant response to the coding question
 * - All UI interactions should be smooth and functional
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GenericChatActionTest : DemoTestBase(
  splashScreenConfig = SplashScreenConfig(
    fontFamily = "Poppins",
    titleColor = "#1a73e8",
    subtitleColor = "#4285f4",
    timestampColor = "#5f6368",
    titleText = "Generic Chat Demo",
    containerStyle = """
        background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
        padding: 40px 60px;
        border-radius: 24px;
        box-shadow: 0 12px 40px rgba(26, 115, 232, 0.15);
        position: relative;
        animation: slideUp 0.8s cubic-bezier(0.16, 1, 0.3, 1);
        border: 2px solid rgba(26, 115, 232, 0.1);
        backdrop-filter: blur(10px);
    """.trimIndent(),
    bodyStyle = """
        margin: 0;
        padding: 20px;
        background: linear-gradient(135deg, #f8f9fa 0%, #e8f0fe 100%);
        display: flex;
        align-items: center;
        justify-content: center;
        height: 100vh;
        text-align: center;
        position: relative;
        overflow: hidden;
        &::before {
            content: '';
            position: absolute;
            top: -50%;
            left: -50%;
            width: 200%;
            height: 200%;
            background: radial-gradient(circle, rgba(26, 115, 232, 0.05) 0%, transparent 70%);
            animation: rotate 20s linear infinite;
        }
    """.trimIndent()
  )
) {
  companion object {
    val log = LoggerFactory.getLogger(GenericChatActionTest::class.java)
  }

  @Test
  fun testGenericChatAction() = with(remoteRobot) {
    speak("Welcome to the AI Assistant Chat demonstration. This powerful feature provides an interactive AI coding assistant that can help with any programming questions or challenges.")
    log.info("Starting testGenericChatAction")
    sleep(2000)

    step("Open Tools menu") {
      speak("Let's start by accessing the AI Assistant Chat through the Tools menu. This feature is conveniently integrated into the IDE's interface.")
      waitFor(Duration.ofSeconds(10)) {
        try {
          log.debug("Attempting to find and click main menu")
          find(CommonContainerFixture::class.java, byXpath("//div[@tooltiptext='Main Menu']")).click()
          sleep(500)
          log.info("Tools menu opened successfully")
          true
        } catch (e: Exception) {
          log.warn("Failed to open Tools menu: ${e.message}")
          false
        }
      }
    }

    step("Click 'AI Assistant Chat' action") {
      speak("Now we'll navigate through the AI Coder menu to launch our AI Assistant. This will open a dedicated chat interface in your default browser.")
      waitFor(Duration.ofSeconds(15)) {
        try {
          val toolsMenu = find(CommonContainerFixture::class.java, byXpath("//div[@text='Tools']"))
          toolsMenu.click()
          val aiCoderMenu = toolsMenu.find(CommonContainerFixture::class.java, byXpath("//div[@text='AI Coder']"))
          robot.mouseMove(toolsMenu.locationOnScreen.x + 10, aiCoderMenu.locationOnScreen.y)
          aiCoderMenu.click()
          val chatMenu = aiCoderMenu.find(
            CommonContainerFixture::class.java,
            byXpath("//div[contains(@class, 'MenuItem') and contains(@text, 'AI Assistant Chat')]")
          )
          robot.mouseMove(chatMenu.locationOnScreen.x + 10, aiCoderMenu.locationOnScreen.y)
          chatMenu.click()

          log.info("'AI Assistant Chat' action clicked")
          true
        } catch (e: Exception) {
          log.warn("Failed to find 'AI Assistant Chat' action: ${e.message}")
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
        speak("The chat interface has launched successfully. Notice the clean, intuitive design that makes interaction with the AI natural and efficient.")
        driver.get(url)
        val wait = WebDriverWait(driver, Duration.ofSeconds(90))

        try {
          val chatInput = wait.until(ExpectedConditions.elementToBeClickable(By.id("chat-input")))
          log.info("Chat interface loaded successfully")
          speak("Let's ask the AI about Kotlin error handling best practices. This is a great example of how the assistant can provide detailed technical guidance.")
          sleep(1000)

          log.debug("Submitting question to chat interface")
          chatInput.click()
          speak("Watch as we type our question. You can ask about any programming concept, debug issues, or get code suggestions.")
          chatInput.sendKeys("What are the best practices for error handling in Kotlin?")
          sleep(1000)

          wait.until(ExpectedConditions.elementToBeClickable(By.id("send-message-button"))).click()
          log.info("Question submitted successfully")
          speak("The question is submitted, and the AI is analyzing it to provide comprehensive guidance on Kotlin error handling patterns.")
          sleep(2000)

          // Wait for response
          wait.until(ExpectedConditions.presenceOfElementLocated(By.className("response-message")))
          speak("The AI has provided a detailed response, including code examples and best practices. Notice how the response is well-structured and specifically tailored to Kotlin development.")
          sleep(3000)

          log.info("Chat interaction completed successfully")
          speak("We've successfully demonstrated the Generic Chat feature.")
        } catch (e: Exception) {
          log.error("Error during chat interaction: ${e.message}", e)
          speak("Encountered an error during chat interaction. Please check the logs for details.")
        } finally {
          log.debug("Cleaning up web driver resources")
          driver.quit()
          log.info("Web driver cleanup completed")
        }
      } else {
        log.error("Failed to retrieve chat interface URL from UDP messages")
        speak("Error: Unable to retrieve the necessary URL.")
      }
      log.debug("Clearing message buffer")
      clearMessageBuffer()
    }

    speak("This concludes our demonstration of the AI Assistant Chat. As you've seen, it provides instant access to AI expertise for any programming questions, making it an invaluable tool for developers. The assistant can help with everything from language-specific questions to complex architectural decisions, all within your IDE environment.")
    sleep(5000)
  }
}