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
class CreateFileFromDescriptionActionTest : DemoTestBase() {
  override fun getTemplateProjectPath(): String {
    return "demo_projects/TestProject"
  }

  companion object {
    val log = LoggerFactory.getLogger(CreateFileFromDescriptionActionTest::class.java)
  }

  @Test
  fun testCreateFileFromDescription() = with(remoteRobot) {
    speak("Welcome to the AI Coder demo. We'll explore the 'Create File from Description' feature, which generates Kotlin files from natural language instructions.")
    log.info("Starting testCreateFileFromDescription")
    sleep(2000)

    step("Open project view") {
      openProjectView()
    }

    step("Open context menu") {
      speak("Navigating to the Kotlin source directory.")
      val projectTree = find(JTreeFixture::class.java, byXpath("//div[@class='ProjectViewTree']"))
      val path = arrayOf(testProjectDir.name, "src", "main", "kotlin")
      projectTree.expandAll(path)
      speak("Opening the context menu to access AI Coder features.")
      projectTree.rightClickPath(*path, fullMatch = false)
      log.info("Context menu opened")
      sleep(2000)
    }
    sleep(3000)

    step("Select 'AI Coder' menu") {
      speak("Selecting the 'AI Coder' option from the context menu.")
      selectAICoderMenu()
    }

    step("Click 'Create File from Description' action") {
      speak("Selecting 'Create File from Description' action.")
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
      speak("Entering a natural language description for a new Kotlin data class.")
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
          speak("Description entered: Create a Kotlin data class named Person with properties: name (String), age (Int), and email (String).")
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
      speak("Verifying file creation in the project structure.")
      val path = arrayOf(testProjectDir.name, "src", "main", "kotlin")
      waitFor(Duration.ofSeconds(20)) {
        remoteRobot.find(JTreeFixture::class.java, byXpath(PROJECT_TREE_XPATH)).apply { expandAll(path) }
        val rows = find(JTreeFixture::class.java, byXpath("//div[@class='ProjectViewTree']")).collectRows()
        val fileCreated = rows.any { it.contains("Person") }
        if (fileCreated) {
          speak("Person file successfully created.")
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
      speak("Opening the created file to examine its contents.")
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
      speak("Verifying the content of the generated file.")
      val editor = find(EditorFixture::class.java, byXpath("//div[@class='EditorComponentImpl']"))
      waitFor(Duration.ofSeconds(5)) {
        val txt = editor.findAllText().joinToString("") { it.text }.replace("\n", "")
        val contentCorrect =
          txt.contains("data class Person(") && txt.contains("val name: String,") && txt.contains("val age: Int,") && txt.contains("val email: String")
        if (contentCorrect) {
          speak("File content verified: Person data class with specified properties created successfully.")
        }
        contentCorrect
      }
      sleep(2000)
    }
    speak("Demo concluded. The 'Create File from Description' feature successfully generated a Kotlin file from natural language input, demonstrating its potential to streamline development processes.")
    sleep(5000)
  }

}