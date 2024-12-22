package com.simiacryptus.aicoder.demotest.action.agent

import com.intellij.remoterobot.fixtures.CommonContainerFixture
import com.intellij.remoterobot.fixtures.JCheckboxFixture
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
 * Tests the Outline Tool functionality of the AI Coder plugin.
 *
 * Prerequisites:
 * - IntelliJ IDEA must be running with the AI Coder plugin installed
 * - A test project must be open
 * - The IDE should be in its default layout
 * - The AI Coder plugin should be properly configured with API keys
 *
 * Test Flow:
 * 1. Opens the Tools menu
 * 2. Selects AI Coder > Outline Tool
 * 3. Configures outline generation settings
 * 4. Launches the outline interface
 * 5. Submits an outline request
 * 6. Verifies outline generation and expansion
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OutlineActionTest : DemoTestBase(
  splashScreenConfig = SplashScreenConfig(
    fontFamily = "JetBrains Mono",
    titleColor = "#1a73e8",
    subtitleColor = "#202124",
    timestampColor = "#5f6368",
    titleText = "Outline Tool Demo",
    containerStyle = """
      background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
      padding: 40px 60px;
      border-radius: 12px;
      box-shadow: 0 8px 24px rgba(26, 115, 232, 0.15);
      border-left: 4px solid #1a73e8;
      animation: slideIn 1.2s cubic-bezier(0.16, 1, 0.3, 1);
      position: relative;
      overflow: hidden;
    """.trimIndent(),
    bodyStyle = """
      margin: 0;
      padding: 20px;
      background: linear-gradient(135deg, #f8f9fa 0%, #e8eaed 100%);
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
        background: repeating-linear-gradient(
          45deg,
          transparent,
          transparent 10px,
          rgba(26, 115, 232, 0.03) 10px,
          rgba(26, 115, 232, 0.03) 20px
        );
      }
    """.trimIndent()
  )
) {

  companion object {
    private val log = LoggerFactory.getLogger(OutlineActionTest::class.java)
  }

  @Test
  fun testOutlineTool() = with(remoteRobot) {
    speak("Welcome to the AI Coder Outline Tool demo.")
    log.info("Starting Outline Tool test")
    sleep(2000)

    step("Open Tools menu") {
      speak("Opening the Tools menu to access the Outline Tool.")
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

    step("Select AI Coder > Outline Tool") {
      speak("Selecting the Outline Tool from the AI Coder menu.")
      // Use more specific selectors and handle each click separately
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

          val outlineMenu = aiCoderMenu.find(
            CommonContainerFixture::class.java,
            byXpath("//div[contains(@class, 'MenuItem') and contains(@text, 'Outline Tool')]")
          )
          robot.mouseMove(outlineMenu.locationOnScreen.x + 10, aiCoderMenu.locationOnScreen.y)
          outlineMenu.click()

          log.info("Outline Tool menu item clicked")
          true
        } catch (e: Exception) {
          log.warn("Failed to click Outline Tool menu item: ${e.message}")
          false
        }
      }
      sleep(1000)
    }

    step("Configure Outline Tool") {
      speak("Configuring Outline Tool settings.")
      waitFor(Duration.ofSeconds(10)) {
        val dialog = find(CommonContainerFixture::class.java, byXpath("//div[@class='MyDialog' and @title='Configure Outline Tool']"))
        if (dialog.isShowing) {
          dialog.find(JCheckboxFixture::class.java, byXpath("//div[@class='JBCheckBox' and @text='Show Projector']")).select()
          dialog.find(JCheckboxFixture::class.java, byXpath("//div[@class='JBCheckBox' and @text='Write Final Essay']")).select()
          speak("Settings configured for optimal outline generation.")
          val okButton = dialog.find(CommonContainerFixture::class.java, byXpath("//div[@class='JButton' and @text='OK']"))
          okButton.click()
          log.info("Outline Tool configured and started")
          true
        } else {
          false
        }
      }
    }

    step("Interact with Outline interface") {
      var url: String? = null
      log.debug("Starting Outline interface interaction")
      waitFor(Duration.ofSeconds(90)) {
        val messages = getReceivedMessages()
        url = messages.firstOrNull { it.startsWith("http") } ?: ""
        log.debug("Searching for URL in messages. Found: ${url?.take(50) ?: "none"}")
        url?.isNotEmpty() ?: false
      }

      if (url != null) {
        log.info("Retrieved Outline interface URL: $url")
        speak("Outline web interface opened.")
        driver.get(url)
        val wait = WebDriverWait(driver, Duration.ofSeconds(90))

        try {
          val chatInput = wait.until(ExpectedConditions.elementToBeClickable(By.id("chat-input")))
          log.info("Outline interface loaded successfully")
          speak("Interface loaded. Submitting outline topic.")
          sleep(1000)

          chatInput.sendKeys("Create an outline for a technical article about AI-assisted coding")
          wait.until(ExpectedConditions.elementToBeClickable(By.id("send-message-button"))).click()
          log.info("Outline request submitted")
          speak("Outline request submitted. Waiting for AI to generate the structure.")
          sleep(5000)

          // Wait for outline generation and expansion
          wait.until(ExpectedConditions.presenceOfElementLocated(By.className("outline-container")))
          speak("Outline generated successfully. You can now expand sections for detailed content.")
          sleep(3000)

          log.info("Outline Tool interaction completed successfully")
        } catch (e: Exception) {
          log.error("Error during Outline Tool interaction: ${e.message}", e)
          speak("Encountered an error during outline generation. Please check the logs for details.")
        } finally {
          driver.quit()
          log.info("Web driver cleanup completed")
        }
      } else {
        log.error("Failed to retrieve Outline interface URL")
        speak("Error: Unable to retrieve the Outline Tool interface URL.")
      }
      clearMessageBuffer()
    }

    speak("Outline Tool demonstration completed. This feature helps create structured content with AI assistance.")
    sleep(5000)
  }
}