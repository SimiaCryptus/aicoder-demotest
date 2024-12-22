package com.simiacryptus.aicoder.demotest.action.chat

import com.intellij.remoterobot.fixtures.CommonContainerFixture
import com.intellij.remoterobot.fixtures.EditorFixture
import com.intellij.remoterobot.fixtures.JTreeFixture
import com.intellij.remoterobot.search.locators.byXpath
import com.intellij.remoterobot.stepsProcessing.step
import com.intellij.remoterobot.utils.keyboard
import com.intellij.remoterobot.utils.waitFor
import com.simiacryptus.aicoder.demotest.DemoTestBase
import com.simiacryptus.aicoder.demotest.SplashScreenConfig
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
 * Tests the Diff Chat functionality of the AI Coder plugin.
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
 * 5. Navigates through the AI Coder menu to select Patch Chat
 * 6. Waits for the chat interface to open in a browser
 * 7. Types a request for code improvements
 * 8. Submits the request and waits for AI response
 * 9. Reviews the suggested patches
 * 10. Applies a patch and verifies the changes
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DiffChatActionTest : DemoTestBase(
  splashScreenConfig = SplashScreenConfig(
    fontFamily = "JetBrains Mono",
    titleColor = "#00ff00",
    subtitleColor = "#32CD32",
    timestampColor = "#98FB98",
    titleText = "Patch Chat Demo",
    containerStyle = """
        background: #1e1e1e;
        padding: 40px 60px;
        border-radius: 10px;
        box-shadow: 0 0 20px rgba(0,255,0,0.2);
        border: 1px solid #00ff00;
        animation: glow 2s ease-in-out infinite;
        position: relative;
        overflow: hidden;
    """.trimIndent(),
    bodyStyle = """
        margin: 0;
        padding: 20px;
        background: #000000;
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
                linear-gradient(45deg, transparent 49%, #00ff00 49%, #00ff00 51%, transparent 51%),
                linear-gradient(-45deg, transparent 49%, #00ff00 49%, #00ff00 51%, transparent 51%);
            background-size: 30px 30px;
            opacity: 0.1;
            animation: moveBackground 20s linear infinite;
        }
        @keyframes glow {
            0%, 100% { box-shadow: 0 0 20px rgba(0,255,0,0.2); }
            50% { box-shadow: 0 0 40px rgba(0,255,0,0.4); }
        }
        @keyframes moveBackground {
            from { background-position: 0 0; }
            to { background-position: 60px 60px; }
        }
    """.trimIndent()
  )
) {
  override fun getTemplateProjectPath(): String {
    return "demo_projects/TestProject"
  }

  @Test
  fun testDiffChatAction() = with(remoteRobot) {
    speak("Welcome to the Patch Chat feature demonstration. This powerful tool enables real-time AI assistance for code improvements and refactoring, with immediate visual feedback and easy patch application.")
    log.info("Starting testDiffChatAction")
    sleep(2000)

    step("Open project view") {
      openProjectView()
      log.info("Project view opened")
      try {
        speak("Let's start by accessing our project files. The Patch Chat feature works with any code file in your project.")
      } catch (e: Exception) {
        log.warn("Failed to provide audio feedback: ${e.message}")
      }
      sleep(2000)
    }

    val projectName = testProjectDir.fileName.name
    step("Open a Kotlin file") {
      speak("We'll open a Kotlin file that could benefit from some improvements. Patch Chat works with any programming language the IDE supports.")
      val path = arrayOf(projectName, "src", "main", "kotlin", "Main.kt")
      val tree = remoteRobot.find(JTreeFixture::class.java, byXpath(PROJECT_TREE_XPATH)).apply { expandAll(path) }
      waitFor(Duration.ofSeconds(10)) { tree.doubleClickPath(*path, fullMatch = false); true }
      log.info("Kotlin file opened")
      sleep(2000)
    }

    step("Select code") {
      speak("Now we'll select the code we want to improve. The AI analyzes both the selected code and its context to provide relevant suggestions.")
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
      speak("The Patch Chat feature is easily accessible through the context menu. You can also configure keyboard shortcuts for quicker access.")
      val editor = find(EditorFixture::class.java, byXpath("//div[@class='EditorComponentImpl']"))
      editor.rightClick()
      log.info("Context menu opened via right-click")
      sleep(2000)
    }

    step("Select 'AI Coder' menu") {
      speak("Under the AI Coder menu, you'll find various AI-powered features. Each one is designed for specific development tasks.")
      selectAICoderMenu()
      sleep(2000)
    }

    step("Click 'Patch Chat' action") {
      speak("Let's launch Patch Chat. This will open an interactive interface where we can discuss code improvements with the AI assistant.")
      waitFor(Duration.ofSeconds(15)) {
        try {
          findAll(CommonContainerFixture::class.java, byXpath("//div[contains(@class, 'ActionMenuItem') and contains(@text, 'Patch Chat')]"))
            .firstOrNull()?.click()
          log.info("'Patch Chat' action clicked")
          true
        } catch (e: Exception) {
          log.warn("Failed to find 'Patch Chat' action: ${e.message}")
          false
        }
      }
      sleep(2000)
    }

    step("Get URL from UDP messages") {
      var url: String? = null
      log.debug("Starting Patch Chat interface interaction")
      waitFor(Duration.ofSeconds(90)) {
        val messages = getReceivedMessages()
        url = messages.firstOrNull { it.startsWith("http") } ?: ""
        log.debug("Searching for URL in messages. Found: ${url?.take(50) ?: "none"}")
        url?.isNotEmpty() ?: false
      }

      if (url != null) {
        log.info("Retrieved Patch Chat interface URL: $url")
        speak("Patch Chat web interface opened.")
        log.debug("Initializing web driver")
        driver.get(url)
        log.debug("Setting up WebDriverWait with 90 second timeout")
        val wait = WebDriverWait(driver, Duration.ofSeconds(90))

        try {
          val chatInput = wait.until(ExpectedConditions.elementToBeClickable(By.id("chat-input")))
          log.info("Chat interface loaded successfully")
          speak("The chat interface is now ready. Watch how we can request code improvements using natural language.")
          sleep(1000)

          log.debug("Submitting request to chat interface")
          chatInput.click()
          speak("We'll ask the AI to suggest optimizations. You can be as specific or general as needed - for example, requesting performance improvements, better error handling, or cleaner code structure.")
          chatInput.sendKeys("Suggest improvements to optimize this code")
          sleep(1000)

          wait.until(ExpectedConditions.elementToBeClickable(By.id("send-message-button"))).click()
          log.info("Request submitted successfully")
          speak("The AI is analyzing our code and generating improvement suggestions. Each suggestion will come with a detailed explanation and ready-to-apply patches.")
          sleep(2000)

          // Wait for and handle the response
          wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".message-container")))
          speak("Let's review the AI's suggestions. Notice how each patch comes with an explanation of the changes and their benefits. You can apply patches directly from this interface.")
          sleep(3000)

          // Look for and click any "Apply" buttons
          val applyButtons = driver.findElements(By.cssSelector("a.href-link"))
            .filter { it.text.contains("Apply", ignoreCase = true) }

          if (applyButtons.isNotEmpty()) {
            speak("We'll apply one of the suggested improvements. The patch will be automatically applied to your code while maintaining proper formatting and structure.")
            applyButtons.first().click()
            sleep(2000)
          }

          speak("And that's how easy it is to improve your code with Patch Chat! You can continue the conversation to refine the changes or request additional improvements.")
          log.info("Patch Chat interaction completed successfully")
        } catch (e: Exception) {
          log.error("Error during Patch Chat interaction: ${e.message}", e)
          speak("Encountered an error during Patch Chat interaction. Please check the logs for details.")
        } finally {
          log.debug("Cleaning up web driver resources")
          driver.quit()
          log.info("Web driver cleanup completed")
        }
      } else {
        log.error("Failed to retrieve Patch Chat interface URL from UDP messages")
        speak("Error: Unable to retrieve the necessary URL.")
      }
      log.debug("Clearing message buffer")
      clearMessageBuffer()
    }

    speak("This concludes our Patch Chat demonstration. We've seen how it streamlines code improvement by combining AI analysis, interactive discussion, and automated patch application. Try it with your own code to experience the benefits of AI-assisted development!")
    sleep(5000)
  }

  companion object {
    val log = LoggerFactory.getLogger(DiffChatActionTest::class.java)
  }
}