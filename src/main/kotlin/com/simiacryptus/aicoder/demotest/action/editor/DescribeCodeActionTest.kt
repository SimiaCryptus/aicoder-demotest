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
 * Tests the Describe Code functionality of the AI Coder plugin.
 *
 * Prerequisites:
 * - IntelliJ IDEA must be running with the AI Coder plugin installed
 * - A test project must be open with Main.kt file
 * - The IDE should be in its default layout
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DescribeCodeActionTest : DemoTestBase(
  splashScreenConfig = SplashScreenConfig(
    fontFamily = "Roboto",
    titleColor = "#00ACC1",
    subtitleColor = "#78909C",
    timestampColor = "#B0BEC5",
    titleText = "Describe Code Demo",
    containerStyle = """
      background: #1E1E1E;
      padding: 40px 60px;
      border-radius: 8px;
      box-shadow: 0 0 30px rgba(0,172,193,0.2);
      border: 1px solid #00ACC1;
      animation: glow 2s ease-in-out infinite alternate;
      position: relative;
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
      font-family: 'JetBrains Mono', monospace;
      @keyframes glow {
        from {
          box-shadow: 0 0 20px rgba(0,172,193,0.2);
        }
        to {
          box-shadow: 0 0 30px rgba(0,172,193,0.6);
        }
      }
      @keyframes fadeIn {
        from { opacity: 0; }
        to { opacity: 1; }
      }
    """.trimIndent()
  )
) {
  companion object {
    private val log = LoggerFactory.getLogger(DescribeCodeActionTest::class.java)
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
  fun testDescribeCode() = with(remoteRobot) {
    speak("Welcome to the Describe Code feature demonstration. This powerful tool helps developers automatically generate clear and comprehensive documentation for their code.")
    log.info("Starting Describe Code test")
    Thread.sleep(2000)

    step("Open project view and file") {
      log.info("Opening project view and navigating to test file")
      speak("Let's start by opening a Kotlin source file that needs documentation.")
      openProjectView()
      val path = arrayOf(testProjectDir.name, "src", "main", "kotlin", "Main.kt")
      log.debug("Navigating to file path: {}", path.joinToString("/"))
      val tree = find(JTreeFixture::class.java, byXpath(PROJECT_TREE_XPATH)).apply { expandAll(path) }
      waitFor(Duration.ofSeconds(10)) { tree.doubleClickPath(*path, fullMatch = false); true }
      Thread.sleep(2000)
      speak("We'll use Main.kt as our example file. Notice how it currently lacks proper documentation.")
    }

    step("Test Describe Code") {
      log.info("Starting Code Description operation")
      speak("To add documentation, we'll first select the code we want to document.")

      val editor = find(EditorFixture::class.java, byXpath("//div[@class='EditorComponentImpl']"))
      selectAllText(editor)
      speak("With our code selected, we can now access the Describe Code feature through the context menu.")
      editor.rightClick(editor.findAllText().firstOrNull()?.point?.location!!)
      Thread.sleep(1000)

      selectAICoderMenu()
      log.debug("Attempting to find and click Describe Code menu item")
      findAll(
        CommonContainerFixture::class.java,
        byXpath("//div[contains(@class, 'ActionMenuItem') and contains(@text, 'Describe Code')]")
      ).firstOrNull()?.click()

      log.debug("Code Description operation triggered")
      speak("Watch as the AI analyzes the code structure and generates appropriate documentation comments. It will identify:")
      Thread.sleep(1000)
      speak("- Function purposes and behaviors")
      Thread.sleep(500)
      speak("- Parameter descriptions and types")
      Thread.sleep(500)
      speak("- Return value details")
      Thread.sleep(500)
      speak("- Usage examples where appropriate")
      Thread.sleep(3000)
    }

    speak("And there we have it! The code is now properly documented with clear, maintainable comments that follow best practices. This documentation will help other developers understand the code's purpose and usage more easily.")
    log.info("Describe Code test completed successfully")
    Thread.sleep(2000)
  }
}