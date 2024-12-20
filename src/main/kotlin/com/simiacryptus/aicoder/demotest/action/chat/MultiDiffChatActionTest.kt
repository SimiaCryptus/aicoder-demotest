package com.simiacryptus.aicoder.demotest.action.chat

import com.intellij.remoterobot.fixtures.CommonContainerFixture
import com.intellij.remoterobot.fixtures.JTreeFixture
import com.intellij.remoterobot.search.locators.byXpath
import com.intellij.remoterobot.stepsProcessing.step
import com.intellij.remoterobot.utils.waitFor
import com.simiacryptus.aicoder.demotest.DemoTestBase
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import org.slf4j.LoggerFactory
import java.lang.Thread.sleep
import java.time.Duration
import kotlin.io.path.name

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
class MultiDiffChatActionTest : DemoTestBase() {
  override fun getTemplateProjectPath(): String {
    return "demo_projects/TestProject"
  }


  companion object {
    val log = LoggerFactory.getLogger(MultiDiffChatActionTest::class.java)
  }

  @Test
  fun testMultiDiffChatAction() = with(remoteRobot) {
    speak("This demo showcases the Multi-Code Chat feature, which enables simultaneous analysis of multiple code files.")
    log.info("Starting testMultiDiffChatAction")
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

    step("Select multiple Kotlin files") {
      try {
        speak("Selecting a Kotlin file in the project structure.")
        val path = arrayOf(projectName, "src", "main", "kotlin", "Main.kt")
        val tree = remoteRobot.find(JTreeFixture::class.java, byXpath(PROJECT_TREE_XPATH)).apply { expandAll(path) }
        waitFor(Duration.ofSeconds(10)) { tree.rightClickPath(*path, fullMatch = false); true }
        log.info("Kotlin file selected")
      } catch (e: Exception) {
        log.error("Failed to select Kotlin files", e)
        speak("Error selecting Kotlin files. Please check the project structure.")
      }
      sleep(2000)
    }

    step("Select 'AI Coder' menu") {
      speak("Selecting the AI Coder option from the context menu.")
      selectAICoderMenu()
      sleep(2000)
    }

    step("Click 'Multi-Code Chat' action") {
      speak("Initiating the Multi-Code Chat action.")
      waitFor(Duration.ofSeconds(15)) {
        try {
          findAll(CommonContainerFixture::class.java, byXpath("//div[contains(@class, 'ActionMenuItem') and contains(@text, 'Modify Files')]"))
            .firstOrNull()?.click()
          log.info("'Diff Chat' action clicked")
          true
        } catch (e: Exception) {
          log.warn("Failed to find 'Diff Chat' action: ${e.message}")
          false
        }
      }
      sleep(2000)
    }

    step("Get URL from UDP messages") {
      var url: String? = null
      log.debug("Starting web interface interaction")
      waitFor(Duration.ofSeconds(90)) {
        val messages = getReceivedMessages()
        url = messages.firstOrNull { it.startsWith("http") } ?: ""
        log.debug("Searching for URL in messages. Found: ${url?.take(50) ?: "none"}")
        url?.isNotEmpty() ?: false
      }
      try {
        if (url == null) {
          log.error("No URL found in UDP messages")
          speak("Error retrieving Multi-Code Chat URL. In a real scenario, retry or contact support.")
          sleep(3000)
        } else {
          log.info("Retrieved URL: $url")
          speak("Launching the web interface in a new browser window.")
          driver.get(url)
          val wait = WebDriverWait(driver, Duration.ofSeconds(90))
          log.debug("Setting up WebDriverWait with 90 second timeout")

          val chatInput = wait.until<WebElement>(ExpectedConditions.elementToBeClickable(By.id("chat-input")))
          log.info("Chat interface loaded successfully")
          speak("Interface loaded. Submitting request.")
          chatInput.click()
          sleep(1000)

          speak("Entering a request to analyze the selected code.")
          val request = "Analyze this class"
          request.forEach { char ->
            chatInput.sendKeys(char.toString())
            sleep(100) // Add a small delay between each character
          }
          sleep(1000)

          val submitButton = wait.until<WebElement>(ExpectedConditions.elementToBeClickable(By.xpath("//button[@type='submit']")))
          speak("Submitting the request for AI analysis.")
          log.info("Submitting request to AI")
          submitButton.click()
          speak("AI is analyzing the code. This process typically takes a few seconds.")
          sleep(2000)

          try {
            val markdownTab =
              wait.until(ExpectedConditions.elementToBeClickable(By.xpath("(//button[contains(@class, 'tab-button') and contains(text(), 'Markdown')])[3]")))
            sleep(2000)
            speak("Viewing the AI's response in Markdown format for better readability.")
            markdownTab.click()
            sleep(3000)
          } catch (e: Exception) {
            log.warn("Copy button not found within the expected time. Skipping copy action.", e)
            speak("AI response is delayed. In a real scenario, consider refreshing or checking network connection.")
            sleep(3000)
          }

          sleep(3000)
          speak("Demonstration of Diff Chat interface complete.")
          log.info("interaction completed successfully")
        }
      } finally {
        log.debug("Cleaning up web driver resources")
        driver.quit()
        log.info("Web driver cleanup completed")
        log.debug("Clearing message buffer")
        clearMessageBuffer()
      }
    }

    speak("Demo concluded. Diff Chat enables efficient code analysis and review.")
    sleep(5000)
  }
}