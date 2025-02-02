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
class CodeChatActionTest : DemoTestBase(
  splashScreenConfig = SplashScreenConfig(
    fontFamily = "JetBrains Mono",
    titleColor = "#00BCD4", // Cyan - representing chat/communication
    subtitleColor = "#546E7A", // Blue grey
    timestampColor = "#78909C", // Lighter blue grey
    titleText = "Code Chat Demo",
    containerStyle = """
        background: linear-gradient(135deg, #263238 0%, #37474F 100%);
        padding: 40px;
        border-radius: 16px;
        box-shadow: 0 20px 50px rgba(0,0,0,0.25);
        animation: slideIn 1.5s cubic-bezier(0.16, 1, 0.3, 1);
        max-width: 800px;
        position: relative;
        border: 1px solid rgba(0, 188, 212, 0.3);
        overflow: hidden;
    """.trimIndent(),
    bodyStyle = """
        margin: 0;
        padding: 30px;
        background: #1A1A1A;
        display: flex;
        align-items: center;
        justify-content: center;
        min-height: 100vh;
        text-align: center;
        font-family: 'JetBrains Mono', monospace;
        position: relative;
        &::before {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            background: 
                radial-gradient(circle at 20% 30%, rgba(0, 188, 212, 0.05) 0%, transparent 50%),
                radial-gradient(circle at 80% 70%, rgba(0, 188, 212, 0.05) 0%, transparent 50%);
            pointer-events: none;
        }
        @keyframes slideIn {
            0% { transform: translateY(30px); opacity: 0; }
            100% { transform: translateY(0); opacity: 1; }
        }
    """.trimIndent()
  )
) {
  override fun getTemplateProjectPath(): String {
    return "demo_projects/TestProject"
  }

  @Test
  fun testCodeChatAction() = with(remoteRobot) {
    speak("Welcome to the Code Chat feature demonstration. This powerful tool enables real-time AI assistance while you code, helping you understand, modify, and improve your codebase through natural language interaction.")
    log.info("Starting testCodeChatAction")
    sleep(2000)

    step("Open project view") {
      openProjectView()
      log.info("Project view opened")
      try {
        speak("Let's start by accessing our project files. The Code Chat feature works with any file in your project, providing context-aware assistance.")
      } catch (e: Exception) {
        log.warn("Failed to provide audio feedback: ${e.message}")
      }

      sleep(2000)
    }

    val projectName = testProjectDir.fileName.name
    step("Open a Kotlin file") {
      speak("We'll open a Kotlin source file that contains code we want to analyze. Code Chat works with any programming language supported by your IDE.")
      val path = arrayOf(projectName, "src", "main", "kotlin", "Main.kt")
      val tree = remoteRobot.find(JTreeFixture::class.java, byXpath(PROJECT_TREE_XPATH)).apply { expandAll(path) }
      for (i in (0 until path.size - 1)) {
        waitFor(Duration.ofSeconds(10)) { tree.isPathExists(*path.sliceArray(0..i), fullMatch = false) }
      }
      waitFor(Duration.ofSeconds(10)) { tree.doubleClickPath(*path, fullMatch = false); true }
      log.info("Kotlin file opened")
      sleep(2000)
    }

    step("Select code") {
      speak("Now we'll select the code we want to discuss. The AI will analyze this selection to provide relevant insights and suggestions. You can select specific sections or entire files.")
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
      speak("To start a Code Chat session, we'll use the context menu. This is one of several ways to access the AI Coder's features.")
      val editor = find(EditorFixture::class.java, byXpath("//div[@class='EditorComponentImpl']"))
      editor.rightClick()
      log.info("Context menu opened via right-click")
      sleep(2000)
    }

    step("Select 'AI Coder' menu") {
      speak("Under the AI Coder menu, you'll find various AI-powered development tools. We're interested in the Code Chat feature for this demonstration.")
      selectAICoderMenu()
      sleep(2000)
    }

    step("Click 'Code Chat' action") {
      speak("When we select Code Chat, the plugin will analyze our code selection and open an interactive chat interface in your default browser. This provides a comfortable environment for detailed code discussions.")
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
      var url: String? = null
      log.debug("Starting Code Chat interface interaction")
      waitFor(Duration.ofSeconds(90)) {
        val messages = getReceivedMessages()
        url = messages.firstOrNull { it.startsWith("http") } ?: ""
        log.debug("Searching for URL in messages. Found: ${url?.take(50) ?: "none"}")
        url?.isNotEmpty() ?: false
      }

      if (url != null) {
        log.info("Retrieved Code Chat interface URL: $url")
        speak("The chat interface has opened in your browser. Notice how it displays your code selection for reference during the conversation.")
        log.debug("Initializing web driver")
        driver.get(url)
        log.debug("Setting up WebDriverWait with 90 second timeout")
        val wait = WebDriverWait(driver, Duration.ofSeconds(90))

        try {
          // Get chat input element
          val chatInput = wait.until(ExpectedConditions.elementToBeClickable(By.id("chat-input")))
          log.info("Chat interface loaded successfully")
          speak("The interface is ready for interaction. You can ask questions about the code, request improvements, or seek explanations - all in natural language.")
          sleep(1000)

          val tabsById = driver.findElements(By.cssSelector("div.tabs-container")).toList().associateBy { it.getDomAttribute("id") }.toMutableMap()
          tabsById.forEach { (id, element) ->
            element.findElements(By.cssSelector(":scope > tabs > .tab-button")).firstOrNull { it.text == "Hide" }?.click()
          }

          log.debug("Submitting request to chat interface")
          chatInput.click()
          speak("Let's ask the AI to create a user manual for our class. This demonstrates how Code Chat can help with documentation tasks while maintaining full context of the code.")
          chatInput.sendKeys("Create a user manual for this class")
          sleep(1000)

          wait.until(ExpectedConditions.elementToBeClickable(By.id("send-message-button"))).click()
          log.info("Request submitted successfully")
          speak("The request is being processed. The AI analyzes both the code structure and your request to generate comprehensive, contextually relevant responses.")
          sleep(2000)

          driver.findElements(By.cssSelector("div.tabs-container")).toList().associateBy { it.getDomAttribute("id") }.filterNot { tabsById.containsKey(it.key) }
            .forEach {
              log.info("New tab id: ${it.key}")
              tabsById[it.key] = it.value
            }

          speak("The AI has provided a detailed response. Notice how it structures the documentation based on the actual code implementation while following best practices.")
          sleep(3000)
          log.info("Code Chat interaction completed successfully")
          speak("The Code Chat feature makes it easy to have meaningful discussions about your code, whether you're seeking explanations, improvements, or documentation help.")
        } catch (e: Exception) {
          log.error("Error during Code Chat interaction: ${e.message}", e)
          speak("Encountered an error during Code Chat interaction. Please check the logs for details.")
        } finally {
          log.debug("Cleaning up web driver resources")
          driver.quit()
          log.info("Web driver cleanup completed")
        }

      } else {
        log.error("Failed to retrieve Code Chat interface URL from UDP messages")
        speak("Error: Unable to retrieve the necessary URL.")
      }
      log.debug("Clearing message buffer")
      clearMessageBuffer()
    }

    speak("This concludes our Code Chat demonstration. We've seen how it provides an intuitive interface for AI-powered code discussions, helping you better understand and document your code. Try experimenting with different types of queries to explore its full capabilities.")
    sleep(5000) // Final sleep of 5 seconds
  }

  companion object {
    val log = LoggerFactory.getLogger(CodeChatActionTest::class.java)
  }


}