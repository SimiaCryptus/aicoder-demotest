package com.simiacryptus.aicoder.demotest.action.generate

import com.intellij.remoterobot.fixtures.CommonContainerFixture
import com.intellij.remoterobot.fixtures.EditorFixture
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
 * Integration test for the Create File from Description action.
 *
 * Prerequisites:
 * - IntelliJ IDEA must be running with the AI Coder plugin installed
 * - A new project named "TestProject" must be created and open
 * - The project must have a standard Kotlin project structure with src/main/kotlin directories
 * - The IDE should be in its default layout with the Project view available
 *
 * Test Flow:
 * 1. Opens the Project view if not already visible
 * 2. Navigates to the src/main/kotlin directory in the project structure
 * 3. Opens the context menu and selects AI Coder > Create File from Description
 * 4. Enters a description for a Person data class
 * 5. Verifies the file is created and contains the expected code
 *
 * Expected Results:
 * - A new Person.kt file should be created in the src/main/kotlin directory
 * - The file should contain a properly formatted Kotlin data class with name, age, and email properties
 * - The file should be automatically opened in the editor
 *
 * Note: This test includes voice narration for demonstration purposes
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CreateFileFromDescriptionActionTest : DemoTestBase(
  splashScreenConfig = SplashScreenConfig(
    fontFamily = "JetBrains Mono",  // Using JetBrains Mono for a coding-focused look
    titleColor = "#00BFA5",  // Vibrant teal color
    subtitleColor = "#26A69A",  // Complementary teal
    timestampColor = "#4DB6AC",  // Lighter teal
    titleText = "Code Generator Demo",
    containerStyle = """
      background: linear-gradient(165deg, #263238 0%, #37474F 100%);
      padding: 40px 60px;
      border-radius: 16px;
      box-shadow: 0 20px 40px rgba(0,0,0,0.3);
      border: 1px solid rgba(0, 191, 165, 0.2);
      animation: pulse 2s infinite ease-in-out;
    """.trimIndent(),
    bodyStyle = """
      margin: 0;
      padding: 30px;
      background: linear-gradient(135deg, #1A237E 0%, #0D47A1 100%);
      display: flex;
      align-items: center;
      justify-content: center;
      min-height: 100vh;
      text-align: center;
      font-family: 'JetBrains Mono', monospace;
      @keyframes pulse {
        0% { transform: scale(1); }
        50% { transform: scale(1.02); }
        100% { transform: scale(1); }
      }
      @keyframes typing {
        from { width: 0 }
        to { width: 100% }
      }
    """.trimIndent()
  )
) {
  override fun getTemplateProjectPath(): String {
    return "demo_projects/TestProject"
  }

  companion object {
    val log = LoggerFactory.getLogger(CreateFileFromDescriptionActionTest::class.java)
  }

  @Test
  fun testCreateFileFromDescription() = with(remoteRobot) {
    speak("Welcome to the AI Coder demo. Today we'll explore how to generate complete code files using natural language descriptions - a powerful feature that helps developers quickly create new code files with proper structure and formatting.")
    log.info("Starting testCreateFileFromDescription")
    sleep(2000)

    step("Open project view") {
      openProjectView()
    }

    step("Open context menu") {
      speak("Let's start by navigating to our project's Kotlin source directory where we'll create our new file.")
      val projectTree = find(JTreeFixture::class.java, byXpath("//div[@class='ProjectViewTree']"))
      val path = arrayOf(testProjectDir.name, "src", "main", "kotlin")
      projectTree.expandAll(path)
      speak("Now we'll access the AI Coder features through the context menu. Notice how it integrates seamlessly with the IDE's standard interface.")
      projectTree.rightClickPath(*path, fullMatch = false)
      log.info("Context menu opened")
      sleep(2000)
    }
    sleep(3000)

    step("Select 'AI Coder' menu") {
      speak("Under the AI Coder menu, you'll find various code generation and analysis tools. We'll focus on file creation today.")
      selectAICoderMenu()
    }

    step("Click 'Create File from Description' action") {
      speak("The Create File from Description action lets us generate complete code files using natural language. It's particularly useful for creating data models, interfaces, and other standard code structures.")
      waitFor(Duration.ofSeconds(15)) {
        try {
          // Find and hover over Generate menu
          findAll(CommonContainerFixture::class.java, byXpath("//div[@text='⚡ Generate']")).firstOrNull()?.moveMouse()
          sleep(1000)
          // Click Create File from Description option
          findAll(
            CommonContainerFixture::class.java,
            byXpath("//div[@class='ActionMenuItem' and contains(@text, 'Create File from Description')]")
          ).firstOrNull()?.click()
          log.info("'Create File from Description' action clicked")
          speak("'Create File from Description' action initiated.")
          true
        } catch (e: Exception) {
          log.warn("Failed to find 'Create File from Description' action: ${e.message}")
          false
        }
      }
      sleep(2000)
    }

    step("Enter file description") {
      speak("Let's create a simple Person data class. Watch how we can describe our requirements in plain English, and the AI will handle the proper Kotlin syntax and conventions.")
      waitFor(Duration.ofSeconds(10)) {
        try {
          val textField = find(JTextAreaFixture::class.java, byXpath("//div[@class='JTextArea']"))
          textField.click()
          remoteRobot.keyboard {
            this.pressing(KeyEvent.VK_CONTROL) {
              key(KeyEvent.VK_A)
            }
            enterText("Create a Kotlin data class named Person with properties: name (String), age (Int), and email (String)")
          }
          speak("I've entered a description requesting a Person class with name, age, and email properties. The AI will determine appropriate types and generate a properly formatted Kotlin data class.")
          log.info("File description entered")
          sleep(2000)
          val okButton = find(CommonContainerFixture::class.java, byXpath("//div[@class='JButton' and @text='Generate']"))
          okButton.click()
          log.info("Generate button clicked")
          true
        } catch (e: Exception) {
          log.warn("Failed to enter file description or click OK: ${e.message}")
          false
        }
      }
      sleep(3000)
    }

    step("Verify file creation") {
      speak("The AI has processed our request and created a new file. Let's verify it appears in our project structure with the correct name and location.")
      val path = arrayOf(testProjectDir.name, "src", "main", "kotlin")
      waitFor(Duration.ofSeconds(20)) {
        remoteRobot.find(JTreeFixture::class.java, byXpath(PROJECT_TREE_XPATH)).apply { expandAll(path) }
        val rows = find(JTreeFixture::class.java, byXpath("//div[@class='ProjectViewTree']")).collectRows()
        val fileCreated = rows.any { it.contains("Person") }
        if (fileCreated) {
          speak("Perfect! The Person.kt file has been created in our source directory, following Kotlin naming conventions.")
        } else {
          log.info(
            """Current rows: ${
              rows.joinToString("\n") {
                it.replace("\n", "\n  ")
              }
            }
            
            """)
        }
        fileCreated
      }
      sleep(2000)
    }

    step("Open created file") {
      speak("Now let's examine the generated code to see how the AI has implemented our requirements.")
      find(JTreeFixture::class.java, byXpath("//div[@class='ProjectViewTree']")).doubleClickPath(
        *arrayOf(
          testProjectDir.name,
          "src",
          "main",
          "kotlin",
          "Person"
        ), fullMatch = false
      )
      sleep(2000)
    }

    step("Verify file content") {
      speak("Let's review the generated code. Notice how the AI has created a proper Kotlin data class with all the requested properties and appropriate type annotations.")
      val editor = find(EditorFixture::class.java, byXpath("//div[@class='EditorComponentImpl']"))
      waitFor(Duration.ofSeconds(5)) {
        val txt = editor.findAllText().joinToString("") { it.text }.replace("\n", "")
        val contentCorrect =
          txt.contains("data class Person(") && txt.contains("val name: String,") && txt.contains("val age: Int,") && txt.contains("val email: String")
        if (contentCorrect) {
          speak("The code is perfectly formatted, following Kotlin conventions. All our requested properties are present with correct types, and the data class syntax is properly implemented.")
        }
        contentCorrect
      }
      sleep(2000)
    }
    speak("And that's how easy it is to generate code files using natural language! This feature saves time, ensures consistent code structure, and helps maintain proper conventions in your project. Try it with more complex requirements like interfaces, service classes, or custom data structures.")
    sleep(5000)
  }

}