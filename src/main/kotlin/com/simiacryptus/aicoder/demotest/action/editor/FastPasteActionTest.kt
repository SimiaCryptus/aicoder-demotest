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
 * Tests the Fast Paste functionality of the AI Coder plugin.
 *
 * Prerequisites:
 * - IntelliJ IDEA must be running with the AI Coder plugin installed
 * - A test project must be open with Main.kt file
 * - The IDE should be in its default layout
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FastPasteActionTest : DemoTestBase(
  splashScreenConfig = SplashScreenConfig(
    fontFamily = "Roboto",
    titleColor = "#1a73e8",
    subtitleColor = "#4285f4",
    timestampColor = "#34a853",
    titleText = "Fast Paste Demo",
    containerStyle = """
      background: linear-gradient(120deg, #ffffff 0%, #f8f9fa 100%);
      padding: 40px 60px;
      border-radius: 20px;
      box-shadow: 0 15px 35px rgba(0,0,0,0.15);
      position: relative;
      overflow: hidden;
      animation: slideIn 1.2s ease-out;
      border: 2px solid #e8f0fe;
    """.trimIndent(),
    bodyStyle = """
      margin: 0;
      padding: 20px;
      background: linear-gradient(135deg, #f8f9fa 0%, #e8f0fe 100%);
      display: flex;
      align-items: center;
      justify-content: center;
      height: 100vh;
      text-align: center;
      position: relative;
      &::before {
        content: '{ }';
        position: absolute;
        font-size: 200px;
        color: rgba(66, 133, 244, 0.05);
        z-index: 0;
        transform: rotate(-15deg);
      }
      &::after {
        content: '< />';
        position: absolute;
        font-size: 180px;
        color: rgba(52, 168, 83, 0.05);
        z-index: 0;
        transform: rotate(15deg);
      }
    """.trimIndent()
  )
) {
  companion object {
    private val log = LoggerFactory.getLogger(FastPasteActionTest::class.java)
  }

  private fun setClipboardContent(text: String) {
    log.debug("Setting clipboard content: {}", text)
    val selection = StringSelection(text)
    Toolkit.getDefaultToolkit().systemClipboard.setContents(selection, selection)
  }

  @Test
  fun testFastPaste() = with(remoteRobot) {
    speak("Welcome to Fast Paste, a powerful feature that instantly converts code between different programming languages while preserving functionality.")
    log.info("Starting Fast Paste test")
    Thread.sleep(2000)

    step("Open project view and file") {
      log.info("Opening project view and navigating to test file")
      speak("Let's start by opening a Kotlin file where we'll demonstrate the conversion capabilities.")
      openProjectView()
      val path = arrayOf(testProjectDir.name, "src", "main", "kotlin", "Main.kt")
      log.debug("Navigating to file path: {}", path.joinToString("/"))
      val tree = find(JTreeFixture::class.java, byXpath(PROJECT_TREE_XPATH)).apply { expandAll(path) }
      waitFor(Duration.ofSeconds(10)) { tree.doubleClickPath(*path, fullMatch = false); true }
      Thread.sleep(2000)
    }

    step("Test Fast Paste") {
      log.info("Starting Fast Paste operation")
      speak("I have some Java code in HTML format on the clipboard. Watch how Fast Paste intelligently handles both the HTML cleanup and Java to Kotlin conversion in one step.")

      setClipboardContent(
        """
                <pre><code>
                public class Example {
                    public static void main(String[] args) {
                        System.out.println("Hello World");
                    }
                }
                </code></pre>
            """.trimIndent()
      )
      log.debug("HTML-wrapped Java code set to clipboard")

      val editor = find(EditorFixture::class.java, byXpath("//div[@class='EditorComponentImpl']"))
      editor.rightClick(editor.findAllText().firstOrNull()?.point?.location!!)
      Thread.sleep(1000)

      selectAICoderMenu()
      log.debug("Attempting to find and click Fast Paste menu item")
      findAll(
        CommonContainerFixture::class.java,
        byXpath("//div[contains(@class, 'ActionMenuItem') and contains(@text, 'Fast Paste')]")
      ).firstOrNull()?.click()

      log.debug("Fast Paste operation triggered")
      speak("Notice how Fast Paste automatically removes the HTML tags and converts the Java code to idiomatic Kotlin, using features like the main function shorthand and println extension function.")
      Thread.sleep(3000)
    }

    speak("With Fast Paste, you can quickly convert code snippets from documentation, web pages, or other sources into your target language, saving time and maintaining code quality.")
    log.info("Fast Paste test completed successfully")
    Thread.sleep(2000)
  }
}