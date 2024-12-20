package com.simiacryptus.aicoder.demotest.action.editor

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
class DescribeCodeActionTest : DemoTestBase() {
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
    speak("Welcome to the AI Coder Describe Code demo.")
    log.info("Starting Describe Code test")
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

    step("Test Describe Code") {
      log.info("Starting Code Description operation")
      speak("Demonstrating the Describe Code feature for adding documentation.")

      val editor = find(EditorFixture::class.java, byXpath("//div[@class='EditorComponentImpl']"))
      selectAllText(editor)
      editor.rightClick(editor.findAllText().firstOrNull()?.point?.location!!)
      Thread.sleep(1000)

      selectAICoderMenu()
      log.debug("Attempting to find and click Describe Code menu item")
      findAll(
        CommonContainerFixture::class.java,
        byXpath("//div[contains(@class, 'ActionMenuItem') and contains(@text, 'Describe Code')]")
      ).firstOrNull()?.click()

      log.debug("Code Description operation triggered")
      speak("AI will now analyze the code and add appropriate documentation comments.")
      Thread.sleep(3000)
    }

    speak("Describe Code demonstration completed.")
    log.info("Describe Code test completed successfully")
    Thread.sleep(2000)
  }
}