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
import java.time.Duration

/**
 * Tests the Shell Command functionality of the AI Coder plugin.
 *
 * Prerequisites:
 * - IntelliJ IDEA must be running with the AI Coder plugin installed
 * - A test project must be open
 * - The IDE should be in its default layout with no dialogs open
 *
 * Test Flow:
 * 1. Opens the Project View if not already visible
 * 2. Selects the project root directory
 * 3. Opens the AI Coder context menu
 * 4. Navigates to Agents > Shell Agent
 * 5. Launches the shell command interface
 * 6. Enters and executes a simple command
 * 7. Verifies the command output
 *
 * Expected Results:
 * - The Shell Command interface should launch successfully
 * - The test command should execute and return output
 * - All UI interactions should be smooth and error-free
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ShellCommandActionTest : DemoTestBase(
  splashScreenConfig = SplashScreenConfig(
    fontFamily = "Source Code Pro",
    titleColor = "#4AF626",
    subtitleColor = "#00ff00",
    timestampColor = "#33ff33",
    titleText = "Shell Command Test",
    containerStyle = """
      background: rgba(0, 0, 0, 0.85);
      padding: 30px 50px;
      border-radius: 8px;
      border: 2px solid #4AF626;
      box-shadow: 0 0 30px rgba(74, 246, 38, 0.3);
      position: relative;
      animation: terminalGlow 2s infinite ease-in-out;
    """.trimIndent(),
    bodyStyle = """
      margin: 0;
      padding: 20px;
      background: #000000;
      background-image: 
        linear-gradient(rgba(0, 30, 0, 0.3) 1px, transparent 1px),
        linear-gradient(90deg, rgba(0, 30, 0, 0.3) 1px, transparent 1px);
      background-size: 20px 20px;
      display: flex;
      align-items: center;
      justify-content: center;
      height: 100vh;
      text-align: center;
      font-family: 'Source Code Pro', monospace;
    """.trimIndent(),
  )
) {

  companion object {
    private val log = LoggerFactory.getLogger(ShellCommandActionTest::class.java)
  }

  @Test
  fun testShellCommand() = with(remoteRobot) {
    speak("Welcome to the AI Coder Shell Command demo.")
    log.info("Starting Shell Command test")
    Thread.sleep(2000)

    step("Open project view") {
      speak("Opening the project view to access the project structure.")
      openProjectView()
      log.info("Project view opened")
      Thread.sleep(2000)
    }

    step("Select project root") {
      speak("Selecting the project root directory.")
      val path = arrayOf(testProjectDir.toFile().name, "src", "main", "kotlin")
      val tree = find(JTreeFixture::class.java, byXpath(PROJECT_TREE_XPATH)).apply { expandAll(path) }
      waitFor(Duration.ofSeconds(10)) {
        tree.rightClickPath(*path, fullMatch = false)
        true
      }
      log.info("Project root selected")
      Thread.sleep(2000)
    }

    step("Navigate to Shell Agent") {
      speak("Navigating to the Shell Agent feature.")
      waitFor(Duration.ofSeconds(10)) {
        try {
          val aiCoderMenu = selectAICoderMenu()
          Thread.sleep(200)
          val agentsMenu = aiCoderMenu.findAll(
            CommonContainerFixture::class.java,
            byXpath("//div[contains(@class, 'Menu') and contains(@text,'Agents')]")
          )
            .firstOrNull() ?: throw Exception("Agents menu not found")
          // Move mouse and wait briefly for submenu to appear
          robot.mouseMove(agentsMenu.locationOnScreen.x + 5, aiCoderMenu.locationOnScreen.y + 5)
          agentsMenu.click()
          Thread.sleep(100)

          // Find and click Shell Agent menu item
          val shellAgentMenu = agentsMenu.findAll(
            CommonContainerFixture::class.java,
            byXpath("//div[contains(@class, 'MenuItem') and contains(@text,'Shell Agent')]")
          ).firstOrNull() ?: throw Exception("Shell Agent menu item not found")
          robot.mouseMove(shellAgentMenu.locationOnScreen.x + 5, agentsMenu.locationOnScreen.y + 5)
          shellAgentMenu.click()
          log.info("Shell Agent menu item clicked")
          true
        } catch (e: Exception) {
          log.warn("Failed to navigate to Shell Agent: ${e.message}")
          false
        }
      }
      Thread.sleep(2000)
    }
    step("Interact with Shell Command interface") {
      var url: String? = null
      log.debug("Starting Shell Command interface interaction")
      waitFor(Duration.ofSeconds(90)) {
        val messages = getReceivedMessages()
        url = messages.firstOrNull { it.startsWith("http") } ?: ""
        url?.isNotEmpty() ?: false
      }

      if (url != null) {
        log.info("Retrieved Shell Command interface URL: $url")
        speak("Shell Command interface opened.")
        driver.get(url)
        val wait = WebDriverWait(driver, Duration.ofSeconds(90))

        try {
          val chatInput = wait.until(ExpectedConditions.elementToBeClickable(By.id("chat-input")))
          log.info("Chat interface loaded")
          speak("Interface loaded. Submitting command.")
          Thread.sleep(1000)

          // Enter command
          val command = if (System.getProperty("os.name").lowercase().contains("windows")) {
            "dir"
          } else {
            "ls -la"
          }
          chatInput.sendKeys(command)
          Thread.sleep(1000)

          // Submit command
          wait.until(ExpectedConditions.elementToBeClickable(By.id("send-message-button"))).click()
          log.info("Command submitted")
          speak("Command submitted. Waiting for execution.")
          Thread.sleep(2000)

          // Wait for response
          wait.until(ExpectedConditions.presenceOfElementLocated(By.className("response-message")))
          speak("Command executed. Output displayed.")
          Thread.sleep(3000)

        } catch (e: Exception) {
          log.error("Error during Shell Command interaction: ${e.message}", e)
          speak("Error encountered during command execution.")
        } finally {
          driver.quit()
          log.info("Browser session closed")
        }
      } else {
        log.error("Failed to retrieve Shell Command interface URL")
        speak("Error: Unable to launch Shell Command interface.")
      }
      clearMessageBuffer()
    }

    speak("Shell Command demonstration completed. This feature enables AI-assisted shell command execution within your project directory.")
    Thread.sleep(5000)
  }
}