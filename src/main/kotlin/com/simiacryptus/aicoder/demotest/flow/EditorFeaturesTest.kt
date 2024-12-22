package com.simiacryptus.aicoder.demotest.flow

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
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.awt.event.KeyEvent
import java.time.Duration
import kotlin.io.path.name

/**
 * Tests the editor-specific features of the AI Coder plugin.
 *
 * Prerequisites:
 * - IntelliJ IDEA must be running with the AI Coder plugin installed
 * - A test project must be open with sample code files
 * - The IDE should be in its default layout
 * - The AI Coder plugin should be properly configured
 *
 * Test demonstrates:
 * 1. Smart Paste functionality
 * 2. Fast Paste functionality
 * 3. Code description and commenting
 * 4. Variable renaming
 * 5. Implementation generation
 * 6. Documentation generation
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EditorFeaturesTest : DemoTestBase(
  splashScreenConfig = SplashScreenConfig(
    fontFamily = "JetBrains Mono",
    titleColor = "#087EA4",
    subtitleColor = "#4CAF50",
    timestampColor = "#FF5722",
    titleText = "Editor Features Demo",
    containerStyle = """
      background: linear-gradient(145deg, #1e1e1e, #2d2d2d);
      padding: 40px;
      border-radius: 15px;
      box-shadow: 0 15px 35px rgba(0,0,0,0.3);
      border: 1px solid #3c3c3c;
      animation: slideIn 1.5s ease-out;
  """.trimIndent(),
    bodyStyle = """
      margin: 0;
      padding: 0;
      display: flex;
      align-items: center;
      justify-content: center;
      height: 100vh;
      text-align: center;
      background-color: #1a1a1a;
      color: #ffffff;
  """.trimIndent()
  )
) {
  private fun setClipboardContent(text: String) {
    log.debug("Setting clipboard content: {}", text)
    val selection = StringSelection(text)
    Toolkit.getDefaultToolkit().systemClipboard.setContents(selection, selection)
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
  fun testEditorFeatures() = with(remoteRobot) {
    speak("Welcome to the AI Coder Editor Features demonstration, where we'll explore powerful tools that enhance your coding workflow.")
    log.info("Starting editor features test suite")
    Thread.sleep(2000)

    step("Open project view and file") {
      log.info("Opening project view and navigating to test file")
      speak("Let's start by opening a sample Kotlin file where we'll demonstrate these features.")
      openProjectView()
      val path = arrayOf(testProjectDir.name, "src", "main", "kotlin", "Main.kt")
      log.debug("Navigating to file path: {}", path.joinToString("/"))
      val tree = find(JTreeFixture::class.java, byXpath(PROJECT_TREE_XPATH)).apply { expandAll(path) }
      waitFor(Duration.ofSeconds(10)) { tree.doubleClickPath(*path, fullMatch = false); true }
      Thread.sleep(2000)
    }
    val editor = find(EditorFixture::class.java, byXpath("//div[@class='EditorComponentImpl']"))
    log.debug("Editor component found")

    fun openEditorContextMenu() {
      log.debug("Opening editor context menu")
      editor.rightClick(editor.findAllText().firstOrNull()?.point?.location!!)
      Thread.sleep(1000)
    }

    step("Test Smart Paste") {
      log.info("Starting Smart Paste test")
      speak("First, let's look at Smart Paste, which intelligently converts code between different programming languages.")
      speak("I'll copy a JavaScript function to the clipboard, and we'll convert it to Kotlin.")
      // Set sample code in clipboard
      setClipboardContent(
        """
                function calculateSum(a, b) {
                    return a + b;
                }
            """.trimIndent()
      )
      log.debug("Sample JavaScript code set to clipboard")

      openEditorContextMenu()
      selectAICoderMenu()
      log.debug("Attempting to find and click Smart Paste menu item")
      findAll(
        CommonContainerFixture::class.java,
        byXpath("//div[contains(@class, 'ActionMenuItem') and contains(@text, 'Smart Paste')]")
      )
        .firstOrNull()?.click()
      log.debug("Smart Paste operation triggered")
      speak("Notice how Smart Paste automatically converts the JavaScript syntax to idiomatic Kotlin code.")
      Thread.sleep(3000) // Wait for AI processing
    }

    step("Test Fast Paste") {
      log.info("Starting Fast Paste test")
      speak("Next, we'll try Fast Paste, which is optimized for quick conversions of simpler code snippets.")
      speak("This time, we'll convert a Java class that's wrapped in HTML markup.")
      // Set HTML content in clipboard
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

      openEditorContextMenu()
      selectAICoderMenu()
      log.debug("Attempting to find and click Fast Paste menu item")
      findAll(
        CommonContainerFixture::class.java,
        byXpath("//div[contains(@class, 'ActionMenuItem') and contains(@text, 'Fast Paste')]")
      ).firstOrNull()?.click()
      log.debug("Fast Paste operation triggered")
      speak("Fast Paste quickly strips the HTML formatting and converts the Java code to Kotlin.")
      Thread.sleep(3000) // Wait for AI processing
    }

    step("Test Code Description") {
      log.info("Starting Code Description test")
      speak("Finally, let's use the Describe Code feature to automatically generate documentation.")
      speak("We'll select all the code and let the AI analyze it to create meaningful comments.")
      selectAllText(editor)
      openEditorContextMenu()
      selectAICoderMenu()
      log.debug("Attempting to find and click Describe Code menu item")
      findAll(
        CommonContainerFixture::class.java,
        byXpath("//div[contains(@class, 'ActionMenuItem') and contains(@text, 'Describe Code')]")
      ).firstOrNull()?.click()
      log.debug("Code Description operation triggered")
      speak("The AI analyzes the code structure and purpose to generate comprehensive documentation comments.")
      Thread.sleep(3000) // Wait for AI processing
    }

    speak("That concludes our demonstration of AI Coder's editor features. These tools help streamline your coding workflow by automating common tasks and maintaining consistent code quality.")
    log.info("Editor features test suite completed successfully")
    Thread.sleep(2000)
  }

  companion object {
    private val log = LoggerFactory.getLogger(EditorFeaturesTest::class.java)
  }
}