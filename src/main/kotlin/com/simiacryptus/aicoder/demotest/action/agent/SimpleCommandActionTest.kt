package com.simiacryptus.aicoder.demotest.action.agent

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
class SimpleCommandActionTest : DemoTestBase(
  splashScreenConfig = SplashScreenConfig(
    fontFamily = "JetBrains Mono",
    titleColor = "#00ACC1",
    subtitleColor = "#78909C",
    timestampColor = "#B0BEC5",
    titleText = "Simple Command Demo",
    containerStyle = """
      background: linear-gradient(145deg, #1E272E, #2C3E50);
      padding: 40px 60px;
      border-radius: 15px;
      box-shadow: 0 15px 35px rgba(0,0,0,0.3);
      position: relative;
      overflow: hidden;
      animation: pulse 2s infinite;
      border: 1px solid rgba(0,172,193,0.3);
    """.trimIndent(),
    bodyStyle = """
      margin: 0;
      padding: 20px;
      background: #121212;
      display: flex;
      align-items: center;
      justify-content: center;
      height: 100vh;
      text-align: center;
      position: relative;
      &::before {
        content: '';
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: 
          radial-gradient(circle at 20% 30%, rgba(0,172,193,0.15) 0%, transparent 50%),
          radial-gradient(circle at 80% 70%, rgba(0,172,193,0.1) 0%, transparent 50%);
        pointer-events: none;
      }
    """.trimIndent()
  )
) {

  companion object {
    val log = LoggerFactory.getLogger(SimpleCommandActionTest::class.java)
  }

  @Test
  fun testSimpleCommand() = with(remoteRobot) {
    speak("Welcome to the Simple Command feature demonstration. This powerful tool allows you to modify code using natural language instructions, making complex code changes as simple as having a conversation.")
    log.info("Starting Simple Command test")
    sleep(2000)

    step("Open project view") {
      speak("Let's start by opening the project view, where we can select the code files we want to modify.")
      openProjectView()
      sleep(2000)
    }

    step("Select directory") {
      speak("We'll select the main source directory containing our Kotlin files. The AI will analyze these files to understand the context before making any changes.")
      val path = arrayOf(testProjectDir.toFile().name, "src", "main", "kotlin")
      val tree = find(JTreeFixture::class.java, byXpath(PROJECT_TREE_XPATH)).apply { expandAll(path) }
      waitFor(Duration.ofSeconds(10)) { tree.rightClickPath(*path, fullMatch = false); true }
      log.info("Directory selected")
      sleep(2000)
    }

    step("Select 'AI Coder' menu") {
      speak("Now we'll access the AI Coder menu, which contains various AI-powered development tools. The Simple Command feature is found under the Agents submenu.")
      selectAICoderMenu()
      sleep(2000)
    }

    step("Click 'Do Something' action") {
      speak("We'll select the 'Do Something' action, which opens our natural language command interface. This is where we can describe the changes we want to make to our code.")
      waitFor(Duration.ofSeconds(10)) {
        try {
          findAll(CommonContainerFixture::class.java, byXpath("//div[@text='AI Coder']//div[@text='\uD83E\uDD16 Agents']"))
            .firstOrNull()?.click()
          sleep(1000)
          findAll(CommonContainerFixture::class.java, byXpath("//div[@text='AI Coder']//div[@text='✨ Do Something']"))
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
          speak("The AI Coder interface opens in your default browser, providing a chat-like experience for code modifications.")
          driver.get(url)
          val wait = WebDriverWait(driver, Duration.ofSeconds(90))

          val chatInput = wait.until(ExpectedConditions.elementToBeClickable(By.id("chat-input")))
          log.info("Chat interface loaded successfully")
          speak("The interface is now ready. Let's demonstrate how to request a common code improvement - adding input validation to our methods.")
          chatInput.click()
          sleep(1000)

          speak("We'll type our request in natural language. Notice how you don't need to know specific code patterns or syntax - just describe what you want to achieve.")
          chatInput.sendKeys("Add input validation to all public methods")
          sleep(1000)

          wait.until(ExpectedConditions.elementToBeClickable(By.id("send-message-button"))).click()
          log.info("Request submitted successfully")
          speak("After submitting our request, the AI analyzes the codebase and generates appropriate validation code for each public method.")
          sleep(2000)

          // Wait for response and show tabs
          try {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".tabs-container")))
            speak("The AI has completed its analysis and generated suggested changes. Each tab represents a different file that needs modifications. Let's review some of these changes.")
            sleep(3000)

            // Click through available tabs
            val tabs = driver.findElements(By.cssSelector(".tabs-container .tab-button"))
            tabs.take(3).forEach { tab ->
              tab.click()
              sleep(2000)
            }
          } catch (e: Exception) {
            log.warn("Response tabs not found: ${e.message}")
            speak("The AI is still processing our request. For complex codebases, thorough analysis may take a few moments to ensure accurate modifications.")
          }

          speak("And that concludes our demonstration of the Simple Command feature. As you've seen, it provides an intuitive way to make systematic code changes across your project using natural language instructions.")
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

    speak("Thank you for watching this demonstration. The Simple Command feature streamlines code modifications by combining AI understanding with your natural language instructions, making complex refactoring tasks simple and efficient.")
    sleep(5000)
  }
}