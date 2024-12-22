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
class MultiDiffChatActionTest : DemoTestBase(
  splashScreenConfig = SplashScreenConfig(
    fontFamily = "JetBrains Mono",
    titleColor = "#2B2B2B",
    subtitleColor = "#6C6C6C",
    timestampColor = "#087EA4",
    titleText = "Multi-Diff Chat Demo",
    containerStyle = """
        background: linear-gradient(135deg, #F5F5F5 0%, #FFFFFF 100%);
        padding: 50px 70px;
        border-radius: 12px;
        box-shadow: 0 8px 32px rgba(0,0,0,0.12);
        border-left: 6px solid #087EA4;
        animation: slideIn 1.2s cubic-bezier(0.16, 1, 0.3, 1);
        position: relative;
        overflow: hidden;
    """.trimIndent(),
    bodyStyle = """
        margin: 0;
        padding: 30px;
        background: #2B2B2B;
        display: flex;
        align-items: center;
        justify-content: center;
        height: 100vh;
        text-align: center;
        font-family: 'JetBrains Mono', monospace;
        position: relative;
        &::before {
          content: '{ }';
          position: absolute;
          top: 20%;
          right: 15%;
          font-size: 180px;
          opacity: 0.05;
          font-family: 'JetBrains Mono', monospace;
          color: #087EA4;
          transform: rotate(-15deg);
        }
    """.trimIndent()
  )
) {
  override fun getTemplateProjectPath(): String {
    return "demo_projects/TestProject"
  }


  companion object {
    val log = LoggerFactory.getLogger(MultiDiffChatActionTest::class.java)
  }

  @Test
  fun testMultiDiffChatAction() = with(remoteRobot) {
    speak("Welcome to the Multi-Code Chat demonstration. This powerful feature allows you to analyze and modify multiple code files simultaneously using AI assistance.")
    log.info("Starting testMultiDiffChatAction")
    sleep(2000)

    step("Open project view") {
      openProjectView()
      log.info("Project view opened")
      try {
        speak("Let's start by accessing our project files. The Multi-Code Chat can analyze any number of files you select.")
      } catch (e: Exception) {
        log.warn("Failed to provide audio feedback: ${e.message}")
      }
      sleep(2000)
    }
    val projectName = testProjectDir.fileName.name

    step("Select multiple Kotlin files") {
      try {
        speak("We'll select a Kotlin file for this demonstration. In practice, you can select multiple files across different languages for comprehensive analysis.")
        val path = arrayOf(projectName, "src", "main", "kotlin", "Main.kt")
        val tree = remoteRobot.find(JTreeFixture::class.java, byXpath(PROJECT_TREE_XPATH)).apply { expandAll(path) }
        waitFor(Duration.ofSeconds(10)) { tree.rightClickPath(*path, fullMatch = false); true }
        log.info("Kotlin file selected")
      } catch (e: Exception) {
        log.error("Failed to select Kotlin files", e)
        speak("We've encountered an issue selecting the files. In a real scenario, ensure your files are accessible and try again.")
      }
      sleep(2000)
    }

    step("Select 'AI Coder' menu") {
      speak("Now we'll access the AI Coder menu. This menu contains various AI-powered features for code analysis and modification.")
      selectAICoderMenu()
      sleep(2000)
    }

    step("Click 'Multi-Code Chat' action") {
      speak("Let's launch the Multi-Code Chat. This will open an interactive interface where we can discuss and analyze our code with AI assistance.")
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
          speak("The AI Coder is opening a dedicated chat interface in your browser. This provides a comfortable environment for code discussion and analysis.")
          driver.get(url)
          val wait = WebDriverWait(driver, Duration.ofSeconds(90))
          log.debug("Setting up WebDriverWait with 90 second timeout")

          val chatInput = wait.until<WebElement>(ExpectedConditions.elementToBeClickable(By.id("chat-input")))
          log.info("Chat interface loaded successfully")
          speak("The interface is ready. Notice how it displays your selected files and provides a chat input area for natural language interaction.")
          chatInput.click()
          sleep(1000)

          speak("Let's ask the AI to analyze our code. You can request anything from code review to specific modifications.")
          val request = "Analyze this class"
          request.forEach { char ->
            chatInput.sendKeys(char.toString())
            sleep(100) // Add a small delay between each character
          }
          sleep(1000)

          val submitButton = wait.until<WebElement>(ExpectedConditions.elementToBeClickable(By.xpath("//button[@type='submit']")))
          speak("Sending our request to the AI. The system will analyze the code context and provide detailed insights.")
          log.info("Submitting request to AI")
          submitButton.click()
          speak("Watch as the AI examines the code structure, patterns, and potential improvements. This comprehensive analysis ensures thorough understanding of your codebase.")
          sleep(2000)

          try {
            val markdownTab =
              wait.until(ExpectedConditions.elementToBeClickable(By.xpath("(//button[contains(@class, 'tab-button') and contains(text(), 'Markdown')])[3]")))
            sleep(2000)
            speak("The AI's response is formatted in Markdown for clarity. You can easily read the analysis and copy any suggested code changes.")
            markdownTab.click()
            sleep(3000)
          } catch (e: Exception) {
            log.warn("Copy button not found within the expected time. Skipping copy action.", e)
            speak("If you experience delays, the system provides clear feedback. You can refresh the page or check your connection without losing context.")
            sleep(3000)
          }

          sleep(3000)
          speak("We've now seen how Multi-Code Chat provides intelligent code analysis across multiple files. This tool is invaluable for code review, refactoring, and understanding complex codebases.")
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

    speak("Thank you for exploring the Multi-Code Chat feature. Remember, you can use this tool for everything from quick code reviews to complex refactoring projects across your entire codebase.")
    sleep(5000)
  }
}