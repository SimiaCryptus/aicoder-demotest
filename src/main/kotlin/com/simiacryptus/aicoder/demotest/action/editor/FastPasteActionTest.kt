package com.simiacryptus.aicoder.demotest.action.editor

import com.intellij.remoterobot.fixtures.CommonContainerFixture
import com.intellij.remoterobot.fixtures.EditorFixture
import com.intellij.remoterobot.fixtures.JTreeFixture
import com.intellij.remoterobot.search.locators.byXpath
import com.intellij.remoterobot.stepsProcessing.step
import com.intellij.remoterobot.utils.waitFor
import com.simiacryptus.aicoder.demotest.DemoTestBase
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
class FastPasteActionTest : DemoTestBase() {
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
    speak("Welcome to the AI Coder Fast Paste demo.")
    log.info("Starting Fast Paste test")
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

    step("Test Fast Paste") {
      log.info("Starting Fast Paste operation")
      speak("Demonstrating the Fast Paste feature for quick code conversion.")

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
      speak("Fast Paste will now convert the Java code to Kotlin syntax.")
      Thread.sleep(3000)
    }

    speak("Fast Paste demonstration completed.")
    log.info("Fast Paste test completed successfully")
    Thread.sleep(2000)
  }
}