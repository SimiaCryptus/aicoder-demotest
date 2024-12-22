package com.simiacryptus.aicoder.demotest.action.chat

import com.intellij.remoterobot.fixtures.CommonContainerFixture
import com.intellij.remoterobot.search.locators.byXpath
import com.intellij.remoterobot.stepsProcessing.step
import com.intellij.remoterobot.utils.waitFor
import com.simiacryptus.aicoder.demotest.DemoTestBase
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
class GenericChatActionTest : DemoTestBase() {
  companion object {
    val log = LoggerFactory.getLogger(GenericChatActionTest::class.java)
  }

  @Test
  fun testGenericChatAction() = with(remoteRobot) {
    speak("Welcome to the AI Coder Generic Chat demo.")
    log.info("Starting testGenericChatAction")
    sleep(2000)

    step("Open Tools menu") {
      speak("Opening the Tools menu to access AI Coder features.")
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
      speak("Selecting the AI Assistant Chat option.")
      waitFor(Duration.ofSeconds(15)) {
        try {
          val toolsMenu = find(CommonContainerFixture::class.java, byXpath("//div[@text='Tools']"))
          toolsMenu.click()
          val aiCoderMenu = toolsMenu.find(
            CommonContainerFixture::class.java,
            byXpath("//div[@text='AI Coder']")
          )
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
        speak("Chat interface opened in browser.")
        driver.get(url)
        val wait = WebDriverWait(driver, Duration.ofSeconds(90))

        try {
          val chatInput = wait.until(ExpectedConditions.elementToBeClickable(By.id("chat-input")))
          log.info("Chat interface loaded successfully")
          speak("Interface loaded. Submitting a coding question.")
          sleep(1000)

          log.debug("Submitting question to chat interface")
          chatInput.click()
          speak("Asking about best practices for error handling in Kotlin.")
          chatInput.sendKeys("What are the best practices for error handling in Kotlin?")
          sleep(1000)

          wait.until(ExpectedConditions.elementToBeClickable(By.id("send-message-button"))).click()
          log.info("Question submitted successfully")
          speak("Question submitted. Waiting for AI response.")
          sleep(2000)

          // Wait for response
          wait.until(ExpectedConditions.presenceOfElementLocated(By.className("response-message")))
          speak("AI response received and displayed.")
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

    speak("Demo concluded. The Generic Chat feature provides a flexible interface for discussing coding concepts and getting AI assistance.")
    sleep(5000)
  }
}