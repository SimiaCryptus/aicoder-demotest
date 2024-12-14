package com.github.simiacryptus.aicoder.demotest.single

import com.github.simiacryptus.aicoder.demotest.BaseActionTest
import com.intellij.remoterobot.fixtures.CommonContainerFixture
import com.intellij.remoterobot.fixtures.JTextAreaFixture
import com.intellij.remoterobot.fixtures.JTreeFixture
import com.intellij.remoterobot.search.locators.byXpath
import com.intellij.remoterobot.stepsProcessing.step
import com.intellij.remoterobot.utils.keyboard
import com.intellij.remoterobot.utils.waitFor
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.slf4j.LoggerFactory
import java.awt.event.KeyEvent
import java.time.Duration

/**
 * UI Test for the Generate Related File action in the AI Coder plugin.
 *
 * Prerequisites:
 * - IntelliJ IDEA must be running with the AI Coder plugin installed
 * - A test project named "TestProject" must be open
 * - The project must contain a README.md file in its root directory
 * - The IDE should be in its default layout with no dialogs open
 *
 * Test Behavior:
 * 1. Opens the Project View if not already visible
 * 2. Locates and selects the README.md file in the project tree
 * 3. Opens the context menu on the README.md file
 * 4. Navigates through the AI Coder menu to select "Generate Related File"
 * 5. Enters a directive to convert the README to a reveal.js presentation
 * 6. Initiates the generation process
 * 7. Verifies that a new presentation.html file is created
 *
 * The test includes voice feedback for demonstration purposes and includes
 * retry logic for potentially flaky UI interactions.
 */

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GenerateRelatedFileActionTest : BaseActionTest() {


    companion object {
        val log = LoggerFactory.getLogger(GenerateRelatedFileActionTest::class.java)
    }

    @Test
    fun testGenerateRelatedFile() = with(remoteRobot) {
        speak("This demo showcases the Generate Related File feature, converting a README.md to a reveal.js HTML presentation.")
        log.info("Starting testGenerateRelatedFile")
        Thread.sleep(3000)

        step("Open project view") {
            speak("Opening the project view.")
            openProjectView()
            Thread.sleep(2000)
        }

        step("Select README.md file") {
            speak("Selecting the README.md file.")
            val path = arrayOf("TestProject", "README.md")
            val tree = remoteRobot.find(JTreeFixture::class.java, byXpath(PROJECT_TREE_XPATH)).apply { expandAll(path) }
            waitFor(Duration.ofSeconds(10)) { tree.clickPath(*path, fullMatch = false); true }
            log.info("README.md file selected")
        }

        step("Open context menu") {
            speak("Opening the context menu.")
            val projectTree = find(JTreeFixture::class.java, byXpath("//div[@class='ProjectViewTree']"))
            projectTree.rightClick()
            log.info("Context menu opened via right-click")
            Thread.sleep(2000)
        }

        step("Select 'AI Coder' menu") {
            speak("Selecting the AI Coder menu.")
            selectAICoderMenu()
        }

        step("Click 'Generate Related File' action") {
            speak("Selecting 'Generate Related File' action.")
            waitFor(Duration.ofSeconds(10)) {
                try {
                    findAll(
                        CommonContainerFixture::class.java,
                        byXpath("//div[contains(@class, 'ActionMenuItem') and contains(@text, 'Generate Related File')]")
                    )
                        .firstOrNull()?.click()
                    log.info("'Generate Related File' action clicked successfully")
                    return@waitFor true
                } catch (e: Exception) {
                    log.warn("Attempt failed: ${e.message}")
                    return@waitFor false
                }
            }
        }

        step("Enter file generation directive") {
            speak("Entering the file generation directive.")
            waitFor(Duration.ofSeconds(10)) {
                try {
                    val textField = find(JTextAreaFixture::class.java, byXpath("//div[@class='JTextArea']"))
                    textField.click()
                    remoteRobot.keyboard {
                        this.pressing(KeyEvent.VK_CONTROL) {
                            key(KeyEvent.VK_A)
                        }
                        enterText("Convert this README.md into a reveal.js HTML presentation")
                    }
                    speak("Directive entered: Convert this README.md into a reveal.js HTML presentation.")
                    log.info("File generation directive entered")
                    Thread.sleep(3000)
                    val okButton = find(CommonContainerFixture::class.java, byXpath("//div[@class='JButton' and @text='Generate']"))
                    okButton.click()
                    log.info("Generate button clicked")
                    speak("Generation process initiated.")
                    true
                } catch (e: Exception) {
                    false
                }
            }
            speak("Waiting for file generation.")
            Thread.sleep(5000)
        }

        step("Verify file creation") {
            speak("Verifying file creation.")
            waitFor(Duration.ofSeconds(20)) {
                val projectTree = find(JTreeFixture::class.java, byXpath("//div[@class='ProjectViewTree']"))
                projectTree.clickPath(*arrayOf("TestProject"), fullMatch = false)
                remoteRobot.keyboard { key(KeyEvent.VK_RIGHT) }
                val fileCreated = projectTree.hasText("presentation.html")
                if (fileCreated) {
                    speak("presentation.html file successfully created.")
                }
                fileCreated
            }
            Thread.sleep(3000)
        }

        speak("Demo concluded. The Generate Related File feature has converted README.md to a reveal.js HTML presentation.")
        Thread.sleep(10000)
    }

    @AfterAll
    fun cleanup() {
        try {
            clearMessageBuffer()
            log.info("Cleanup completed successfully")
        } catch (e: Exception) {
            log.error("Cleanup failed", e)
        }
    }
}