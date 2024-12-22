package com.simiacryptus.aicoder.demotest.action.generate

import com.intellij.remoterobot.fixtures.CommonContainerFixture
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
import org.slf4j.LoggerFactory
import java.awt.event.KeyEvent
import java.lang.Thread.sleep
import java.time.Duration
import kotlin.io.path.name

/**
 * Integration test for the Generate Documentation action in the AI Coder plugin.
 *
 * Prerequisites:
 * - IntelliJ IDEA must be running with the AI Coder plugin installed
 * - The DataGnome project must be open and loaded
 * - The project structure must contain the path: src/main/kotlin/com.simiacryptus.util/files
 * - The IDE should be in its default layout with no dialogs open
 *
 * Test Behavior:
 * 1. Opens the Project View if not already visible
 * 2. Navigates to the files utility package in the project structure
 * 3. Right-clicks to open the context menu
 * 4. Selects AI Coder > Generate Documentation
 * 5. Configures documentation generation with specific instructions
 * 6. Waits for and verifies the documentation generation
 * 7. Confirms the result appears in the editor
 *
 * Expected Results:
 * - Documentation should be generated for the files utility package
 * - The generated documentation should appear in a new editor window
 * - The process should complete within 60 seconds
 *
 * Note: This test includes voice feedback for demonstration purposes
 */

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GenerateDocumentationActionTest : DemoTestBase(
  splashScreenConfig = SplashScreenConfig(
    fontFamily = "Fira Code",
    titleColor = "#007ACC", // IntelliJ-like blue
    subtitleColor = "#6F42C1", // Purple for AI emphasis
    timestampColor = "#28A745", // Success green
    titleText = "Documentation Generator Demo",
    containerStyle = """
      background: linear-gradient(135deg, #1E1E1E 0%, #2D2D2D 100%);
      padding: 50px 70px;
      border-radius: 15px;
      box-shadow: 0 20px 40px rgba(0,0,0,0.3);
      border: 1px solid #3C3C3C;
      animation: pulse 2s infinite;
    """.trimIndent(),
    bodyStyle = """
      margin: 0;
      padding: 25px;
      background-color: #1E1E1E;
      background-image: 
        radial-gradient(circle at 25px 25px, #333 2%, transparent 0%),
        radial-gradient(circle at 75px 75px, #333 2%, transparent 0%);
      background-size: 100px 100px;
      display: flex;
      align-items: center;
      justify-content: center;
      height: 100vh;
      font-family: 'Fira Code', monospace;
      position: relative;
      overflow: hidden;
    """.trimIndent(),
  )
) {

  companion object {
    val log = LoggerFactory.getLogger(GenerateDocumentationActionTest::class.java)
  }

  override fun getTemplateProjectPath(): String {
    return "demo_projects/DataGnome"
  }

  override fun waitAfterProjectOpen() {
    sleep(45000)
  }

  @Test
  fun testGenerateDocumentation() = with(remoteRobot) {
    speak("This demo showcases the AI Coder's Generate Documentation feature for automatic API documentation creation.")
    try {
      log.info("Starting testGenerateDocumentation")
      sleep(3000)

      step("Open project view") {
        speak("Opening the project view to access files.")
        openProjectView()
      }
      step("Navigate to files utility package") {
        speak("Navigating to the files utility package for documentation generation.")
        log.info("Files utility package selected")
        sleep(2000)
      }

      step("Navigate to files utility package") {
        speak("Navigating to the files utility package for documentation generation.")
        //val path = arrayOf(testProjectDir.name, "src", "main", "kotlin", "com.simiacryptus.util.files")
        val path = arrayOf(testProjectDir.name, "src", "main", "kotlin", "com", "simiacryptus", "util", "files")
        val tree = remoteRobot.find(JTreeFixture::class.java, byXpath(PROJECT_TREE_XPATH)).apply { expandAll(path) }
        waitFor(Duration.ofSeconds(10)) { tree.rightClickPath(*path, fullMatch = false); true }
        log.info("Files utility package selected")
        sleep(2000)
      }

      step("Select 'AI Coder' menu") {
        speak("Selecting the AI Coder option from the context menu.")
        selectAICoderMenu()
        sleep(2000)
      }

      step("Click 'Generate Documentation' action") {
        speak("Initiating the 'Generate Documentation' action.")
        waitFor(Duration.ofSeconds(15)) {
          try {
            // Find and hover over Generate menu
            findAll(CommonContainerFixture::class.java, byXpath("//div[@text='⚡ Generate']"))
              .firstOrNull()?.moveMouse()
            sleep(1000)
            findAll(
              CommonContainerFixture::class.java,
              byXpath("//div[@class='ActionMenuItem' and contains(@text, 'Generate Documentation')]")
            )

            // Click Generate Documentation option
            findAll(CommonContainerFixture::class.java, byXpath("//div[@class='ActionMenuItem' and contains(@text, 'Generate Documentation')]"))
              .firstOrNull()?.click()
            log.info("'Generate Documentation' action clicked")
            true
          } catch (e: Exception) {
            log.warn("Failed to find 'Generate Documentation' action: ${e.message}")
            false
          }
        }
        sleep(2000)
      }

      step("Configure documentation generation") {
        val DIALOG_TITLE = "Compile Documentation"
        speak("Configuring documentation generation settings.")
        waitFor(Duration.ofSeconds(10)) {
          val dialog = find(CommonContainerFixture::class.java, byXpath("//div[@class='MyDialog' and @title='$DIALOG_TITLE']"))
          dialog.isShowing
        }
        val dialog = find(
          CommonContainerFixture::class.java,
          byXpath("//div[@class='MyDialog' and @title='$DIALOG_TITLE']")
        )
        val aiInstructionField = dialog.find(JTextAreaFixture::class.java, byXpath("//div[@class='JBTextArea']"))
        aiInstructionField.click()
        keyboard {
          pressing(KeyEvent.VK_CONTROL) {
            key(KeyEvent.VK_A) // Select all
            key(KeyEvent.VK_BACK_SPACE) // Delete
          }
        }
        remoteRobot.keyboard { enterText("Create comprehensive API documentation for the files utility package") }

        try {
          val generateButton = find(CommonContainerFixture::class.java, byXpath("//div[@class='JButton' and @text='OK']"))
          generateButton.click()
          log.info("Documentation generation configured and started")
          speak("Documentation generation initiated with custom instructions.")
          true
        } catch (e: Exception) {
          log.warn("Failed to configure documentation generation: ${e.message}")
          false
        }
      }
      speak("AI is processing the request. This process typically takes a few seconds, depending on package size and complexity.")
      sleep(5000)

      step("Verify documentation creation")
      {
        speak("Verifying documentation creation and editor display.")
        waitFor(Duration.ofSeconds(600)) {
          try {
            val editor = find(CommonContainerFixture::class.java, byXpath("//div[@class='EditorCompositePanel']"))
            if (editor.isShowing) {
              speak("Documentation successfully generated and displayed in the editor.")
              true
            } else {
              false
            }
          } catch (e: Exception) {
            log.warn("Failed to find opened editor: ${e.message}")
            false
          }
        }
        sleep(3000)
        speak("Demo complete. The Generate Documentation feature has created comprehensive API documentation for the files utility package, demonstrating its efficiency in automating documentation tasks.")
      }
    } finally {
      clearMessageBuffer()
    }
  }

}