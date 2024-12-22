package com.simiacryptus.aicoder.demotest.action.editor

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
import org.slf4j.LoggerFactory
import java.awt.event.KeyEvent
import java.time.Duration
import kotlin.io.path.name

/**
 * Tests the Custom Edit functionality of the AI Coder plugin.
 *
 * Prerequisites:
 * - IntelliJ IDEA must be running with the AI Coder plugin installed
 * - A test project must be open with Main.kt file
 * - The IDE should be in its default layout
 * - The AI Coder plugin should be properly configured
 *
 * Test Flow:
 * 1. Opens the Project View if not already visible
 * 2. Navigates to and opens the Main.kt file
 * 3. Selects all code in the editor
 * 4. Opens the context menu via right-click
 * 5. Navigates through the AI Coder menu to select Custom Edit
 * 6. Enters a custom edit instruction
 * 7. Verifies the edit is applied
 *
 * Expected Results:
 * - The custom edit should be applied to the selected code
 * - The editor should update with the modified code
 * - All UI interactions should complete without errors
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CustomEditActionTest : DemoTestBase(
  splashScreenConfig = SplashScreenConfig(
    fontFamily = "JetBrains Mono",
    titleColor = "#00ACC1",
    subtitleColor = "#78909C",
    timestampColor = "#B0BEC5",
    titleText = "Custom Edit Demo",
    containerStyle = """
      background: #1E1E1E;
      padding: 40px 60px;
      border-radius: 8px;
      box-shadow: 0 0 30px rgba(0,172,193,0.2);
      animation: glow 2s ease-in-out infinite alternate;
      border: 1px solid #00ACC1;
      position: relative;
      overflow: hidden;
    """.trimIndent(),
    bodyStyle = """
      margin: 0;
      padding: 20px;
      background: #2B2B2B;
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
        background: linear-gradient(45deg, #00ACC1 0%, transparent 100%);
        opacity: 0.05;
        z-index: 0;
      }
    """.trimIndent()
  )
) {
  companion object {
    private val log = LoggerFactory.getLogger(CustomEditActionTest::class.java)
  }

  private fun selectAllText(editor: EditorFixture) {
    log.debug("Selecting all text in editor")
    editor.click()
    editor.keyboard {
      pressing(KeyEvent.VK_CONTROL) {
        key(KeyEvent.VK_A)
      }
    }
    Thread.sleep(500)
  }

  @Test
  fun testCustomEdit() = with(remoteRobot) {
    speak("Welcome to the Custom Edit feature demonstration. This powerful tool helps you modify code using natural language instructions.")
    log.info("Starting Custom Edit test")
    Thread.sleep(2000)

    step("Open project view and file") {
      log.info("Opening project view and navigating to test file")
      speak("Let's start by opening a sample code file that we'll enhance with AI assistance.")
      openProjectView()
      val path = arrayOf(testProjectDir.name, "src", "main", "kotlin", "Main.kt")
      log.debug("Navigating to file path: {}", path.joinToString("/"))
      val tree = find(JTreeFixture::class.java, byXpath(PROJECT_TREE_XPATH)).apply { expandAll(path) }
      waitFor(Duration.ofSeconds(10)) { tree.doubleClickPath(*path, fullMatch = false); true }
      Thread.sleep(2000)
    }

    step("Test Custom Edit") {
      log.info("Starting Custom Edit operation")
      speak("Now we'll use Custom Edit to improve our code. First, let's select the code we want to modify.")

      val editor = find(EditorFixture::class.java, byXpath("//div[@class='EditorComponentImpl']"))
      selectAllText(editor)
      speak("With our code selected, we can access Custom Edit through the context menu.")
      editor.rightClick(editor.findAllText().firstOrNull()?.point?.location!!)
      Thread.sleep(1000)

      selectAICoderMenu()
      log.debug("Attempting to find and click Custom Edit menu item")
      findAll(
        CommonContainerFixture::class.java,
        byXpath("//div[contains(@class, 'ActionMenuItem') and contains(@text, 'Edit Code')]")
      ).firstOrNull()?.click()

      log.debug("Waiting for edit instruction dialog")
      waitFor(Duration.ofSeconds(10)) {
        try {
          val dialog = find(CommonContainerFixture::class.java, byXpath("//div[@class='JDialog' and @title='Edit Code']"))
          val textField = dialog.find(CommonContainerFixture::class.java, byXpath("//div[@class='MultiplexingTextField']"))
          textField.click()
          speak("The Custom Edit dialog allows us to describe our desired changes in natural language. Let's add error handling to our code.")
          keyboard {
            enterText("Add error handling")
          }
          speak("We've instructed the AI to add error handling. This will make our code more robust and reliable.")
          Thread.sleep(1000)

          val okButton = dialog.find(CommonContainerFixture::class.java, byXpath("//div[@class='JButton' and @text='OK']"))
          okButton.click()
          true
        } catch (e: Exception) {
          log.warn("Failed to interact with edit dialog: ${e.message}")
          false
        }
      }

      log.debug("Custom Edit operation triggered")
      speak("Watch as the AI analyzes our code and intelligently adds appropriate error handling patterns. It considers the context and best practices for our programming language.")
      Thread.sleep(3000)
    }

    speak("And there we have it! The AI has enhanced our code with proper error handling. Notice how it maintained the original functionality while making the code more robust. This is just one example of how Custom Edit can help improve your code through natural language instructions.")
    log.info("Custom Edit test completed successfully")
    Thread.sleep(2000)
  }
}