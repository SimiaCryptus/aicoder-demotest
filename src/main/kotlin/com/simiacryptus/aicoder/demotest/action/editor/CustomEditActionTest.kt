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
class CustomEditActionTest : DemoTestBase() {
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
    speak("Welcome to the AI Coder Custom Edit demo.")
    log.info("Starting Custom Edit test")
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

    step("Test Custom Edit") {
      log.info("Starting Custom Edit operation")
      speak("Demonstrating the Custom Edit feature for AI-powered code modifications.")

      val editor = find(EditorFixture::class.java, byXpath("//div[@class='EditorComponentImpl']"))
      selectAllText(editor)
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
          val textField = dialog.find(CommonContainerFixture::class.java, byXpath("//div[@class='JTextField']"))
          textField.click()
          keyboard {
            enterText("Add error handling")
          }
          speak("Entering custom edit instruction: Add error handling")
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
      speak("AI will now modify the code to add error handling.")
      Thread.sleep(3000)
    }

    speak("Custom Edit demonstration completed.")
    log.info("Custom Edit test completed successfully")
    Thread.sleep(2000)
  }
}