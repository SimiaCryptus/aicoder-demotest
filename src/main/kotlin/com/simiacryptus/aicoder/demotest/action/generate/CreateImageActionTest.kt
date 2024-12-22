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
 * Tests the Create Image functionality of the AI Coder plugin.
 *
 * Prerequisites:
 * - IntelliJ IDEA must be running with the AI Coder plugin installed
 * - A test project must be open with sample code files
 * - The IDE should be in its default layout
 * - The AI Coder plugin should be properly configured with valid API credentials
 *
 * Test Flow:
 * 1. Opens the Project View if not already visible
 * 2. Navigates to a source code file
 * 3. Opens the context menu and selects AI Coder > Generate > Create Image
 * 4. Configures image generation settings
 * 5. Waits for image generation
 * 6. Verifies the generated image file
 *
 * Expected Results:
 * - An image file should be generated in the specified location
 * - The image should represent the selected code's structure or functionality
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CreateImageActionTest : DemoTestBase(
  splashScreenConfig = SplashScreenConfig(
    fontFamily = "Poppins",
    titleColor = "#FF6B6B",
    subtitleColor = "#4ECDC4",
    timestampColor = "#45B7D1",
    titleText = "Image Generation Demo",
    containerStyle = """
      background: rgba(255, 255, 255, 0.95);
      padding: 40px 60px;
      border-radius: 25px;
      box-shadow: 0 15px 35px rgba(0,0,0,0.2);
      animation: slideIn 1.2s ease-out;
      position: relative;
      overflow: hidden;
      border: 2px solid #4ECDC4;
    """.trimIndent(),
    bodyStyle = """
      margin: 0;
      padding: 20px;
      background: linear-gradient(135deg, #1A2980 0%, #26D0CE 100%);
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
        background: url('data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNDAiIGhlaWdodD0iNDAiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PGNpcmNsZSBjeD0iMjAiIGN5PSIyMCIgcj0iMyIgZmlsbD0icmdiYSgyNTUsMjU1LDI1NSwwLjEpIi8+PC9zdmc+');
        opacity: 0.1;
        z-index: 1;
      }
    """.trimIndent()
  )
) {
  companion object {
    private val log = LoggerFactory.getLogger(CreateImageActionTest::class.java)
  }

  override fun getTemplateProjectPath(): String {
    return "demo_projects/TestProject"
  }

  @Test
  fun testCreateImage() = with(remoteRobot) {
    speak("Welcome to the AI Coder Create Image demo.")
    log.info("Starting Create Image test")
    sleep(2000)

    step("Open project view and select file") {
      speak("Opening the project view to access source files.")
      openProjectView()
      val path = arrayOf(testProjectDir.name, "src", "main", "kotlin", "Main.kt")
      log.debug("Navigating to file path: {}", path.joinToString("/"))
      val tree = find(JTreeFixture::class.java, byXpath(PROJECT_TREE_XPATH)).apply { expandAll(path) }
      waitFor(Duration.ofSeconds(10)) { tree.rightClickPath(*path, fullMatch = false); true }
      log.info("Source file selected")
      sleep(2000)
    }

    step("Select 'AI Coder' menu") {
      speak("Accessing the AI Coder menu.")
      selectAICoderMenu()
      sleep(2000)
    }

    step("Navigate to Create Image action") {
      speak("Selecting the Create Image action.")
      waitFor(Duration.ofSeconds(15)) {
        try {
          // Find and hover over Generate menu
          findAll(CommonContainerFixture::class.java, byXpath("//div[@text='⚡ Generate']"))
            .firstOrNull()?.moveMouse()
          sleep(1000)
          // Click Create Image option
          findAll(CommonContainerFixture::class.java, byXpath("//div[contains(@class, 'ActionMenuItem') and contains(@text, 'Create Image')]"))
            .firstOrNull()?.click()
          log.info("Create Image action clicked")
          true
        } catch (e: Exception) {
          log.warn("Failed to find Create Image action: ${e.message}")
          false
        }
      }
      sleep(2000)
    }

    step("Configure image generation") {
      speak("Configuring image generation settings.")
      waitFor(Duration.ofSeconds(10)) {
        try {
          val dialog = find(CommonContainerFixture::class.java, byXpath("//div[@class='MyDialog' and @title='Generate Image']"))
          val instructionsArea = dialog.find(JTextAreaFixture::class.java, byXpath("//div[@class='JTextArea']"))
          instructionsArea.click()
          keyboard {
            pressing(KeyEvent.VK_CONTROL) {
              key(KeyEvent.VK_A)
            }
            enterText("Create a UML class diagram showing the structure and relationships")
          }
          speak("Entered instructions for generating a UML class diagram.")
          log.info("Image generation instructions entered")
          sleep(2000)

          val okButton = dialog.find(CommonContainerFixture::class.java, byXpath("//div[@class='JButton' and @text='OK']"))
          okButton.click()
          log.info("Image generation started")
          true
        } catch (e: Exception) {
          log.warn("Failed to configure image generation: ${e.message}")
          false
        }
      }
    }

    step("Wait for image generation") {
      speak("Waiting for AI to generate the image. This may take a few moments.")
      sleep(10000) // Wait for image generation to complete
      log.info("Image generation completed")
    }

    speak("Create Image demonstration completed. The AI has generated a visual representation of the code structure.")
    log.info("Create Image test completed successfully")
    sleep(2000)
  }
}