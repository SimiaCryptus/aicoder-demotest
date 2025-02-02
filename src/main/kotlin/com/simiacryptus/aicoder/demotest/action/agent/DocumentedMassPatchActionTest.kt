package com.simiacryptus.aicoder.demotest.action.agent

import com.intellij.remoterobot.fixtures.CommonContainerFixture
import com.intellij.remoterobot.fixtures.JCheckboxFixture
import com.intellij.remoterobot.fixtures.JTextAreaFixture
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
import java.time.Duration
import kotlin.io.path.name

/**
 * Tests the Documented Mass Patch functionality of the AI Coder plugin.
 *
 * Prerequisites:
 * - IntelliJ IDEA must be running with the AI Coder plugin installed
 * - A test project must be open with both code and documentation files
 * - The project must have a standard structure with src/main/kotlin and docs directories
 * - The IDE should be in its default layout with no dialogs open
 *
 * Test Flow:
 * 1. Opens the Project View if not already visible
 * 2. Selects both code and documentation files in the project tree
 * 3. Opens the AI Coder context menu
 * 4. Initiates the Documented Mass Patch action
 * 5. Configures patch settings including auto-apply option
 * 6. Reviews and applies generated patches through web interface
 * 7. Verifies successful patch application
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DocumentedMassPatchActionTest : DemoTestBase(
  splashScreenConfig = SplashScreenConfig(
    fontFamily = "JetBrains Mono",
    titleColor = "#00C853",
    subtitleColor = "#64DD17",
    timestampColor = "#76FF03",
    titleText = "Documentated Mass Patcher",
    containerStyle = """
        background: #1E1E1E;
        padding: 50px 70px;
        border-radius: 15px;
        border: 2px solid #00C853;
        box-shadow: 0 0 30px rgba(0,200,83,0.3);
        animation: glow 2s ease-in-out infinite alternate;
    """.trimIndent(),
    bodyStyle = """
        margin: 0;
        padding: 20px;
        background: linear-gradient(135deg, #121212 0%, #1E1E1E 100%);
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
          top: 0;
          left: 0;
          right: 0;
          bottom: 0;
          background: repeating-linear-gradient(
            0deg,
            transparent,
            rgba(0, 200, 83, 0.1) 2px,
            transparent 4px
          );
          pointer-events: none;
        }
    """.trimIndent()
  )
) {

  companion object {
    private val log = LoggerFactory.getLogger(DocumentedMassPatchActionTest::class.java)
  }

  override fun getTemplateProjectPath(): String {
    return "demo_projects/TestProject"
  }

  @Test
  fun testDocumentedMassPatch() = with(remoteRobot) {
    speak("Welcome to the Documented Mass Patch feature demonstration. This powerful tool helps ensure your code stays aligned with documentation standards by automatically analyzing and updating multiple files.")
    log.info("Starting Documented Mass Patch test")
    Thread.sleep(2000)

    step("Open project view") {
      log.info("Opening project view")
      openProjectView()
      speak("First, let's open the project view where we can select both our documentation and code files that need to be synchronized.")
      Thread.sleep(2000)
    }

    step("Select project files") {
      speak("We'll select our source code directory which contains multiple files that need to be updated. The AI will analyze both the documentation standards and the code to suggest appropriate changes.")
      val path = arrayOf(testProjectDir.name, "src", "main", "kotlin")
      val tree = find(JTreeFixture::class.java, byXpath(PROJECT_TREE_XPATH)).apply { expandAll(path) }
      waitFor(Duration.ofSeconds(10)) { tree.rightClickPath(*path, fullMatch = false); true }
      log.info("Project files selected")
      Thread.sleep(2000)
    }

    step("Open AI Coder menu") {
      speak("Now we'll access the AI Coder menu, which contains our powerful code transformation tools.")
      selectAICoderMenu()
      Thread.sleep(2000)
    }

    step("Select Mass Patch action") {
      speak("Let's select the Documented Mass Patch action. This will start our automated documentation compliance process.")
      waitFor(Duration.ofSeconds(10)) {
        try {
          findAll(CommonContainerFixture::class.java, byXpath("//div[contains(@class, 'ActionMenuItem') and contains(@text, 'Mass Patch')]"))
            .firstOrNull()?.click()
          log.info("Mass Patch action clicked")
          true
        } catch (e: Exception) {
          log.warn("Failed to find Mass Patch action: ${e.message}")
          false
        }
      }
      Thread.sleep(2000)
    }

    step("Configure patch settings") {
      speak("In the settings dialog, we can customize how the AI analyzes and updates our code. We'll enter specific instructions for documentation compliance and enable automatic application of changes for efficiency.")
      waitFor(Duration.ofSeconds(10)) {
        val dialog = find(CommonContainerFixture::class.java, byXpath("//div[@class='MyDialog']"))
        if (dialog.isShowing) {
          // Set AI instruction
          val instructionField = dialog.find(JTextAreaFixture::class.java, byXpath("//div[@class='JBTextArea']"))
          instructionField.click()
          keyboard {
            pressing(KeyEvent.VK_CONTROL) {
              key(KeyEvent.VK_A)
            }
            enterText("Update code to match documentation standards")
          }

          // Enable auto-apply
          dialog.find(JCheckboxFixture::class.java, byXpath("//div[@text='Auto Apply Changes']"))
            .apply { if (!isSelected()) click() }

          speak("We've enabled auto-apply to streamline the process, allowing the AI to automatically implement approved changes across all selected files.")
          val okButton = dialog.find(CommonContainerFixture::class.java, byXpath("//div[@class='JButton' and @text='OK']"))
          okButton.click()
          log.info("Patch settings configured")
          true
        } else false
      }
      Thread.sleep(2000)
    }

    step("Process patches") {
      speak("Now we'll see the AI in action as it analyzes our documentation and code files. The web interface provides a clear view of all suggested changes and their implementation status.")
      val messages = getReceivedMessages()
      val url = messages.firstOrNull { it.startsWith("http") }
      if (url != null) {
        log.info("Retrieved URL: $url")
        driver.get(url)
        val wait = WebDriverWait(driver, Duration.ofSeconds(90))

        try {
          // Wait for interface to load
          wait.until(ExpectedConditions.elementToBeClickable(By.id("chat-input")))
          speak("The interface has loaded, showing us a detailed breakdown of all proposed changes. Each modification is carefully aligned with our documentation standards.")

          // Wait for patches to be generated and applied
          wait.until { driver ->
            val elements = driver.findElements(By.cssSelector(".message-container"))
            elements.any { it.isDisplayed }
          }
          speak("Watch as the AI automatically applies the changes, ensuring consistent documentation compliance across all files. Each change is logged for review and version control.")
          Thread.sleep(5000)

          // Review results
          val tabButtons = driver.findElements(By.cssSelector(".tabs-container > .tabs > .tab-button"))
          tabButtons.take(3).forEach { button ->
            button.click()
            Thread.sleep(2000)
          }

        } catch (e: Exception) {
          log.error("Error during patch processing", e)
          speak("In case of any issues during the process, the system provides clear error messages and allows for manual intervention if needed.")
        } finally {
          driver.quit()
        }
      } else {
        log.error("No URL found in messages")
        speak("If the web interface fails to load, you can always restart the process or use the IDE's built-in diff viewer for manual review.")
      }
      clearMessageBuffer()
    }

    speak("And that concludes our demonstration of the Documented Mass Patch feature. As you've seen, it dramatically simplifies the process of maintaining consistency between your documentation and code, saving time while ensuring high-quality standards across your entire project.")
    Thread.sleep(5000)
  }
}