package com.simiacryptus.aicoder.demotest.action.agent

import com.intellij.remoterobot.fixtures.CommonContainerFixture
import com.intellij.remoterobot.fixtures.JTreeFixture
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
 * Tests the Simple Command functionality of the AI Coder plugin.
 *
 * Prerequisites:
 * - IntelliJ IDEA must be running with the AI Coder plugin installed
 * - A test project must be open with a standard structure
 * - The IDE should be in its default layout with no dialogs open
 *
 * Test Flow:
 * 1. Opens the Project View if not already visible
 * 2. Selects a directory in the project structure
 * 3. Opens the AI Coder context menu
 * 4. Launches the Simple Command feature
 * 5. Interacts with the web interface to:
 *    - Submit a coding task
 *    - Monitor execution progress
 *    - Review and apply generated changes
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SimpleCommandActionTest : DemoTestBase() {

  companion object {
    val log = LoggerFactory.getLogger(SimpleCommandActionTest::class.java)
  }

  @Test
  fun testSimpleCommand() = with(remoteRobot) {
    speak("Welcome to the AI Coder Simple Command demo. This feature enables natural language code modifications.")
    log.info("Starting Simple Command test")
    sleep(2000)

    step("Open project view") {
      speak("Opening the project view to access the project structure.")
      openProjectView()
      sleep(2000)
    }

    step("Select directory") {
      speak("Selecting a directory to apply the command.")
      val path = arrayOf(testProjectDir.toFile().name, "src", "main", "kotlin")
      val tree = find(JTreeFixture::class.java, byXpath(PROJECT_TREE_XPATH)).apply { expandAll(path) }
      waitFor(Duration.ofSeconds(10)) { tree.rightClickPath(*path, fullMatch = false); true }
      log.info("Directory selected")
      sleep(2000)
    }

    step("Select 'AI Coder' menu") {
      speak("Accessing the AI Coder menu.")
      selectAICoderMenu()
      sleep(2000)
    }

    step("Click 'Do Something' action") {
      speak("Selecting the 'Do Something' action.")
      waitFor(Duration.ofSeconds(10)) {
        try {
          findAll(CommonContainerFixture::class.java, byXpath("//div[contains(@class, 'ActionMenuItem') and contains(@text, 'Agents')]"))
            .firstOrNull()?.click()
          sleep(1000)
          findAll(CommonContainerFixture::class.java, byXpath("//div[contains(@class, 'ActionMenuItem') and contains(@text, 'Do Something')]"))
            .firstOrNull()?.click()
          log.info("'Do Something' action clicked")
          true
        } catch (e: Exception) {
          log.warn("Failed to find 'Do Something' action: ${e.message}")
          false
        }
      }
      sleep(2000)
    }

    step("Interact with web interface") {
      var url: String? = null
      log.debug("Starting web interface interaction")
      waitFor(Duration.ofSeconds(90)) {
        val messages = getReceivedMessages()
        url = messages.firstOrNull { it.startsWith("http") } ?: ""
        log.debug("Searching for URL in messages. Found: ${url?.take(50) ?: "none"}")
        url?.isNotEmpty() ?: false
      }

      try {
        if (url != null) {
          log.info("Retrieved URL: $url")
          speak("Opening the web interface in a browser window.")
          driver.get(url)
          val wait = WebDriverWait(driver, Duration.ofSeconds(90))

          val chatInput = wait.until(ExpectedConditions.elementToBeClickable(By.id("chat-input")))
          log.info("Chat interface loaded successfully")
          speak("Interface loaded. Submitting request.")
          chatInput.click()
          sleep(1000)

          speak("Entering a request to add input validation.")
          chatInput.sendKeys("Add input validation to all public methods")
          sleep(1000)

          wait.until(ExpectedConditions.elementToBeClickable(By.id("send-message-button"))).click()
          log.info("Request submitted successfully")
          speak("Request submitted. Waiting for AI response.")
          sleep(2000)

          // Wait for response and show tabs
          try {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".tabs-container")))
            speak("AI has analyzed the code and proposed changes.")
            sleep(3000)

            // Click through available tabs
            val tabs = driver.findElements(By.cssSelector(".tabs-container .tab-button"))
            tabs.take(3).forEach { tab ->
              tab.click()
              sleep(2000)
            }
          } catch (e: Exception) {
            log.warn("Response tabs not found: ${e.message}")
            speak("AI response is taking longer than expected.")
          }

          speak("Simple Command feature demonstration complete.")
          log.info("Web interface interaction completed successfully")
        } else {
          log.error("No URL found in UDP messages")
          speak("Error retrieving web interface URL.")
        }
      } finally {
        log.debug("Cleaning up web driver resources")
        driver.quit()
        log.info("Web driver cleanup completed")
        clearMessageBuffer()
      }
    }

    speak("Demo concluded. The Simple Command feature enables natural language code modifications with AI assistance.")
    sleep(5000)
  }
}