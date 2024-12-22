package com.simiacryptus.aicoder.demotest.action.editor

import com.intellij.remoterobot.fixtures.CommonContainerFixture
import com.intellij.remoterobot.fixtures.EditorFixture
import com.intellij.remoterobot.fixtures.JTreeFixture
import com.intellij.remoterobot.search.locators.byXpath
import com.intellij.remoterobot.stepsProcessing.step
import com.intellij.remoterobot.utils.waitFor
import com.simiacryptus.aicoder.demotest.DemoTestBase
import com.simiacryptus.aicoder.demotest.SplashScreenConfig
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.slf4j.LoggerFactory
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.time.Duration
import kotlin.io.path.name

/**
 * Tests the Smart Paste functionality of the AI Coder plugin.
 *
 * Prerequisites:
 * - IntelliJ IDEA must be running with the AI Coder plugin installed
 * - A test project must be open with Main.kt file
 * - The IDE should be in its default layout
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SmartPasteActionTest : DemoTestBase(
  splashScreenConfig = SplashScreenConfig(
    fontFamily = "JetBrains Mono",
    titleColor = "#00BCD4",
    subtitleColor = "#4CAF50",
    timestampColor = "#9E9E9E",
    titleText = "Smart Paste Demo",
    containerStyle = """
        background: #1E1E1E;
        padding: 40px 60px;
        border-radius: 15px;
        box-shadow: 0 0 30px rgba(0,188,212,0.3);
        position: relative;
        animation: slideIn 1.5s ease-out;
        border: 2px solid #00BCD4;
    """.trimIndent(),
    bodyStyle = """
        margin: 0;
        padding: 20px;
        background: linear-gradient(135deg, #2B2B2B 0%, #1E1E1E 100%);
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
            background: repeating-linear-gradient(45deg, transparent, transparent 10px, rgba(0,188,212,0.05) 10px, rgba(0,188,212,0.05) 20px);
            animation: backgroundScroll 20s linear infinite;
        }
    """.trimIndent()
  )
) {
  companion object {
    private val log = LoggerFactory.getLogger(SmartPasteActionTest::class.java)
  }

  private fun setClipboardContent(text: String) {
    log.debug("Setting clipboard content: {}", text)
    val selection = StringSelection(text)
    Toolkit.getDefaultToolkit().systemClipboard.setContents(selection, selection)
  }

  @Test
  fun testSmartPaste() = with(remoteRobot) {
    speak("Welcome to the AI Coder Smart Paste demo.")
    log.info("Starting Smart Paste test")
    Thread.sleep(2000)

    step("Open project view and file") {
      log.info("Opening project view and navigating to test file")
      openProjectView()
      val path = arrayOf(testProjectDir.name, "src", "main", "kotlin", "Main.kt")
      log.debug("Navigating to file path: {}", path.joinToString("/"))
      val tree = find(JTreeFixture::class.java, byXpath(PROJECT_TREE_XPATH)).apply { expandAll(path) }
      waitFor(Duration.ofSeconds(10)) { tree.doubleClickPath(*path, fullMatch = false); true }
      Thread.sleep(2000)
    }

    step("Test Smart Paste") {
      log.info("Starting Smart Paste operation")
      speak("Demonstrating the Smart Paste feature for converting JavaScript to Kotlin.")

      setClipboardContent(
        """
                function calculateSum(a, b) {
                    return a + b;
                }
            """.trimIndent()
      )
      log.debug("Sample JavaScript code set to clipboard")

      val editor = find(EditorFixture::class.java, byXpath("//div[@class='EditorComponentImpl']"))
      editor.rightClick(editor.findAllText().firstOrNull()?.point?.location!!)
      Thread.sleep(1000)

      selectAICoderMenu()
      log.debug("Attempting to find and click Smart Paste menu item")
      findAll(
        CommonContainerFixture::class.java,
        byXpath("//div[contains(@class, 'ActionMenuItem') and contains(@text, 'Smart Paste')]")
      ).firstOrNull()?.click()

      log.debug("Smart Paste operation triggered")
      speak("Smart Paste will now convert the JavaScript function to Kotlin syntax.")
      Thread.sleep(3000)
    }

    speak("Smart Paste demonstration completed.")
    log.info("Smart Paste test completed successfully")
    Thread.sleep(2000)
  }
}