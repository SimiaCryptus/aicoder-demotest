package com.simiacryptus.aicoder.demotest.flow

import com.intellij.remoterobot.fixtures.CommonContainerFixture
import com.intellij.remoterobot.fixtures.EditorFixture
import com.intellij.remoterobot.fixtures.JTreeFixture
import com.intellij.remoterobot.search.locators.byXpath
import com.intellij.remoterobot.stepsProcessing.step
import com.intellij.remoterobot.utils.keyboard
import com.intellij.remoterobot.utils.waitFor
import com.simiacryptus.aicoder.demotest.DemoTestBase
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
class EditorFeaturesTest : DemoTestBase() {
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
    speak("Welcome to the AI Coder Editor Features demo.")
    log.info("Starting editor features test suite")
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
    val editor = find(EditorFixture::class.java, byXpath("//div[@class='EditorComponentImpl']"))
    log.debug("Editor component found")

    fun openEditorContextMenu() {
      log.debug("Opening editor context menu")
      editor.rightClick(editor.findAllText().firstOrNull()?.point?.location!!)
      Thread.sleep(1000)
    }

    step("Test Smart Paste") {
      log.info("Starting Smart Paste test")
      speak("Demonstrating the Smart Paste feature.")
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
      Thread.sleep(3000) // Wait for AI processing
    }

    step("Test Fast Paste") {
      log.info("Starting Fast Paste test")
      speak("Now testing the Fast Paste feature for quick code conversion.")
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
      Thread.sleep(3000) // Wait for AI processing
    }

    step("Test Code Description") {
      log.info("Starting Code Description test")
      speak("Adding documentation comments to the code.")
      selectAllText(editor)
      openEditorContextMenu()
      selectAICoderMenu()
      log.debug("Attempting to find and click Describe Code menu item")
      findAll(
        CommonContainerFixture::class.java,
        byXpath("//div[contains(@class, 'ActionMenuItem') and contains(@text, 'Describe Code')]")
      ).firstOrNull()?.click()
      log.debug("Code Description operation triggered")
      Thread.sleep(3000) // Wait for AI processing
    }

    speak("Editor features demonstration completed.")
    log.info("Editor features test suite completed successfully")
    Thread.sleep(2000)
  }

  companion object {
    private val log = LoggerFactory.getLogger(EditorFeaturesTest::class.java)
  }
}