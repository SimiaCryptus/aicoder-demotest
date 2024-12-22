package com.simiacryptus.aicoder.demotest.action.chat

/**
 * Tests the Multi-Code Chat functionality in the AI Coder plugin.
 *
 * Prerequisites:
 * - IntelliJ IDEA must be running with the AI Coder plugin installed
 * - A test project must be open with the following structure:
 *   TestProject/
 *     src/
 *       main/
 *         kotlin/
 *           Person.kt
 * - The IDE should be in its default layout with no dialogs open
 *
 * Test Flow:
 * 1. Opens the Project View if not already visible
 * 2. Navigates to and selects the Person.kt file in the project structure
 * 3. Opens the context menu and selects AI Coder > Multi-Code Chat
 * 4. Launches a browser window with the chat interface
 * 5. Submits a code analysis request and waits for response
 * 6. Demonstrates UI interactions with the response
 * 7. Closes the browser and cleans up
 *
 * Expected Results:
 * - The Multi-Code Chat interface should launch successfully
 * - The AI should provide an analysis of the selected code
 * - All UI interactions should be smooth and functional
 */
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

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MultiCodeChatActionTest : DemoTestBase(
  splashScreenConfig = SplashScreenConfig(
    fontFamily = "JetBrains Mono",
    titleColor = "#00B4E7", // IntelliJ-inspired blue
    subtitleColor = "#6B7C93",
    timestampColor = "#A9B7C6", // IntelliJ editor text color
    titleText = "Multi-Code Chat Demo",
    containerStyle = """
      background: linear-gradient(145deg, #2B2B2B 0%, #3C3F41 100%);
      padding: 40px 60px;
      border-radius: 12px;
      box-shadow: 0 20px 40px rgba(0, 0, 0, 0.4);
      border: 1px solid #4D4D4D;
      animation: slideIn 1.5s cubic-bezier(0.16, 1, 0.3, 1);
    """.trimIndent(),
    bodyStyle = """
      margin: 0;
      padding: 20px;
      background: #1E1E1E;
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
        background: repeating-linear-gradient(
          45deg,
          #2B2B2B 0%,
          #2B2B2B 10%,
          #1E1E1E 10%,
          #1E1E1E 20%
        );
        opacity: 0.1;
        animation: backgroundMove 20s linear infinite;
      }
    """.trimIndent()
  )
) {
  override fun getTemplateProjectPath(): String {
    return "demo_projects/TestProject"
  }


  companion object {
    val log = LoggerFactory.getLogger(MultiCodeChatActionTest::class.java)
  }

  @Test
  fun testMultiCodeChatAction() = with(remoteRobot) {
    speak("Welcome to the Multi-Code Chat feature demonstration. This powerful tool allows you to analyze multiple code files simultaneously using AI assistance.")
    log.info("Starting testMultiCodeChatAction")
    sleep(2000)

    step("Open project view") {
      openProjectView()
      log.info("Project view opened")
      try {
        speak("Let's start by accessing our project files. The Multi-Code Chat feature works best when analyzing related code files together.")
      } catch (e: Exception) {
        log.warn("Failed to provide audio feedback: ${e.message}")
      }
      sleep(2000)
    }
    val projectName = testProjectDir.fileName.name

    step("Select multiple Kotlin files") {
      try {
        speak("We'll select our main Kotlin file. In practice, you can select multiple files to analyze their relationships and interactions.")
        val path = arrayOf(projectName, "src", "main", "kotlin", "Main.kt")
        val tree = remoteRobot.find(JTreeFixture::class.java, byXpath(PROJECT_TREE_XPATH)).apply { expandAll(path) }
        waitFor(Duration.ofSeconds(10)) { tree.rightClickPath(*path, fullMatch = false); true }
        log.info("Kotlin file selected")
      } catch (e: Exception) {
        log.error("Failed to select Kotlin files", e)
        speak("If you encounter issues selecting files, ensure they are accessible and you have proper permissions.")
      }
      sleep(2000)
    }

    step("Select 'AI Coder' menu") {
      speak("Now we'll access the AI Coder menu. This contains all our AI-powered development tools.")
      selectAICoderMenu()
      sleep(2000)
    }

    step("Click 'Multi-Code Chat' action") {
      speak("Let's launch the Multi-Code Chat interface. This will open a dedicated chat window for code analysis and discussion.")
      waitFor(Duration.ofSeconds(15)) {
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
      sleep(2000)
    }

    step("Get URL from UDP messages") {
      var url: String? = null
      log.debug("Starting Multi-Code Chat interface interaction")
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
          speak("The chat interface is opening in your default browser. This provides a familiar environment for interacting with the AI assistant.")
          driver.get(url)
          val wait = WebDriverWait(driver, Duration.ofSeconds(90))
          log.debug("Setting up WebDriverWait with 90 second timeout")

          val chatInput = wait.until<WebElement>(ExpectedConditions.elementToBeClickable(By.id("chat-input")))
          log.info("Chat interface loaded successfully")
          speak("The interface has loaded successfully. Notice the clean, intuitive design that makes code discussions natural and efficient.")
          chatInput.click()
          sleep(1000)

          speak("Let's ask the AI to analyze our code. You can request anything from basic code review to complex architectural analysis.")
          val request = "Analyze this class"
          request.forEach { char ->
            chatInput.sendKeys(char.toString())
            sleep(100) // Add a small delay between each character
          }
          sleep(1000)

          val submitButton = wait.until<WebElement>(ExpectedConditions.elementToBeClickable(By.xpath("//button[@type='submit']")))
          speak("Sending our request to the AI. The assistant will analyze the code context and provide detailed insights.")
          log.info("Submitting request to AI")
          submitButton.click()
          speak("Watch as the AI processes our request. It's examining the code structure, patterns, and potential improvements.")
          sleep(2000)

          try {
            val markdownTab =
              wait.until(ExpectedConditions.elementToBeClickable(By.xpath("(//button[contains(@class, 'tab-button') and contains(text(), 'Markdown')])[3]")))
            sleep(2000)
            speak("The AI's response is formatted in Markdown, making it easy to read and understand. You can copy code snippets, follow links, and navigate through the analysis.")
            markdownTab.click()
            sleep(3000)
          } catch (e: Exception) {
            log.warn("Copy button not found within the expected time. Skipping copy action.", e)
            speak("AI response is delayed. In a real scenario, consider refreshing or checking network connection.")
            sleep(3000)
          }

          sleep(3000)
          speak("We've now seen how Multi-Code Chat streamlines code review and analysis. This tool is particularly valuable for understanding complex codebases and ensuring consistent code quality.")
          log.info("Multi-Code Chat interaction completed successfully")
        }
      } finally {
        log.debug("Cleaning up web driver resources")
        driver.quit()
        log.info("Web driver cleanup completed")
        log.debug("Clearing message buffer")
        clearMessageBuffer()
      }
    }

    speak("That concludes our demonstration of Multi-Code Chat. Remember, you can use this tool for everything from quick code reviews to deep architectural discussions. The AI assistant helps you understand and improve your code more efficiently than ever.")
    sleep(5000)
  }
}