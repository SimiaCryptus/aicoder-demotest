package com.simiacryptus.aicoder.demotest.action.plan

import com.intellij.remoterobot.fixtures.CommonContainerFixture
import com.intellij.remoterobot.fixtures.JCheckboxFixture
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
 * Tests the Single Task functionality of the AI Coder plugin.
 *
 * Prerequisites:
 * - IntelliJ IDEA must be running with the AI Coder plugin installed
 * - A test project must be open with a standard structure
 * - The IDE should be in its default layout with no dialogs open
 *
 * Test Flow:
 * 1. Opens the Project View if not already visible
 * 2. Navigates to the source directory in the project tree
 * 3. Opens the AI Coder context menu
 * 4. Launches the Single Task feature
 * 5. Configures Single Task settings
 * 6. Interacts with the web interface to:
 *    - Submit a simple task request
 *    - Monitor execution
 *    - View results
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SingleTaskActionTest : DemoTestBase() {

  override fun getTemplateProjectPath(): String {
    return "demo_projects/TestProject"
  }

  companion object {
    private val log = LoggerFactory.getLogger(SingleTaskActionTest::class.java)
  }

  @Test
  fun testSingleTaskAction() = with(remoteRobot) {
    speak("Welcome to the AI Coder Single Task demonstration. This feature enables quick execution of simple coding tasks.")
    log.info("Starting Single Task test")
    sleep(3000)

    step("Open project view") {
      speak("Opening the project view to access files.")
      openProjectView()
      sleep(2000)
    }

    step("Select source directory") {
      speak("Selecting the source directory.")
      val path = arrayOf(testProjectDir.toFile().name, "src", "main", "kotlin")
      val tree = find(JTreeFixture::class.java, byXpath(PROJECT_TREE_XPATH)).apply { expandAll(path) }
      waitFor(Duration.ofSeconds(10)) { tree.rightClickPath(*path, fullMatch = false); true }
      log.info("Source directory selected")
      sleep(3000)
    }

    step("Select 'AI Coder' menu") {
      speak("Opening the AI Coder menu.")
      selectAICoderMenu()
      sleep(2000)
    }

    step("Click 'Single Task' action") {
      speak("Selecting the Single Task feature.")
      waitFor(Duration.ofSeconds(10)) {
        try {
          // Navigate to Task Plans submenu
          findAll(CommonContainerFixture::class.java, byXpath("//div[@text='📋 Task Plans']"))
            .firstOrNull()?.moveMouse()
          sleep(1000)
          // Click Single Task option
          findAll(CommonContainerFixture::class.java, byXpath("//div[contains(@text, 'Single Task')]"))
            .firstOrNull()?.click()
          log.info("Single Task action clicked")
          true
        } catch (e: Exception) {
          log.warn("Failed to find Single Task action: ${e.message}")
          false
        }
      }
      sleep(2000)
    }

    step("Configure Single Task") {
      speak("Configuring Single Task settings.")
      waitFor(Duration.ofSeconds(10)) {
        val dialog = find(CommonContainerFixture::class.java, byXpath("//div[@class='MyDialog']"))
        if (dialog.isShowing) {
          dialog.find(JCheckboxFixture::class.java, byXpath("//div[@text='Auto-apply fixes']")).apply {
            if (!isSelected()) {
              click()
              speak("Enabled auto-apply fixes option.")
            }
          }
          val okButton = dialog.find(CommonContainerFixture::class.java, byXpath("//div[@class='JButton' and @text='OK']"))
          okButton.click()
          log.info("Single Task configured")
          true
        } else {
          false
        }
      }
    }

    step("Interact with web interface") {
      var url: String? = null
      waitFor(Duration.ofSeconds(90)) {
        val messages = getReceivedMessages()
        url = messages.firstOrNull { it.startsWith("http") } ?: ""
        url?.isNotEmpty() ?: false
      }

      if (url != null) {
        log.info("Retrieved URL: $url")
        speak("Opening the Single Task interface.")
        driver.get(url)
        val wait = WebDriverWait(driver, Duration.ofSeconds(90))

        try {
          val chatInput = wait.until(ExpectedConditions.elementToBeClickable(By.id("chat-input")))
          speak("Submitting a simple task request.")
          chatInput.sendKeys("Create a function to calculate the factorial of a number")
          sleep(1000)

          val submitButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("send-message-button")))
          submitButton.click()
          speak("Request submitted. Waiting for AI response.")
          sleep(5000)

          // Wait for response
          wait.until(ExpectedConditions.presenceOfElementLocated(By.className("response-message")))
          speak("AI has provided a response with the implementation.")
          sleep(3000)
        } catch (e: Exception) {
          log.error("Error during Single Task interaction: ${e.message}", e)
          speak("Encountered an error during task execution.")
        } finally {
          driver.quit()
        }
      } else {
        log.error("Failed to retrieve interface URL")
        speak("Error retrieving the interface URL.")
      }
      clearMessageBuffer()
    }

    speak("Single Task demonstration completed. This feature provides a streamlined way to execute simple coding tasks.")
    sleep(5000)
  }
}