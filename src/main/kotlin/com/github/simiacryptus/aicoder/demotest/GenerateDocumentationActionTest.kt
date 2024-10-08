package com.github.simiacryptus.aicoder.demotest

import com.intellij.remoterobot.fixtures.CommonContainerFixture
import com.intellij.remoterobot.fixtures.JTextAreaFixture
import com.intellij.remoterobot.fixtures.JTreeFixture
import com.intellij.remoterobot.search.locators.byXpath
import com.intellij.remoterobot.stepsProcessing.step
import com.intellij.remoterobot.utils.keyboard
import com.intellij.remoterobot.utils.waitFor
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.slf4j.LoggerFactory
import java.awt.event.KeyEvent
import java.lang.Thread.sleep
import java.time.Duration

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GenerateDocumentationActionTest : BaseActionTest() {


    companion object {
        val log = LoggerFactory.getLogger(GenerateDocumentationActionTest::class.java)
    }
    @Test
    fun testGenerateDocumentation() = with(remoteRobot) {
    TestUtil.speak("This demo showcases the AI Coder's Generate Documentation feature for automatic API documentation creation.")
        log.info("Starting testGenerateDocumentation")
        sleep(3000)

        step("Open project view") {
        TestUtil.speak("Opening the project view to access files.")
            find(CommonContainerFixture::class.java, byXpath("//div[@class='ProjectViewTree']")).click()
            log.info("Project view opened")
            sleep(2000)
        }

        step("Navigate to files utility package") {
        TestUtil.speak("Navigating to the files utility package for documentation generation.")
            val projectTree = find(JTreeFixture::class.java, byXpath("//div[@class='ProjectViewTree']"))
            projectTree.rightClickPath(*arrayOf("DataGnome", "src", "main", "kotlin", "com.simiacryptus.util", "files"), fullMatch = false)
            log.info("Files utility package selected")
            sleep(2000)
        }

        step("Select 'AI Coder' menu") {
        TestUtil.speak("Selecting the AI Coder option from the context menu.")
            waitFor(Duration.ofSeconds(10)) {
                try {
                    val aiCoderMenu = find(CommonContainerFixture::class.java, byXpath("//div[contains(@class, 'ActionMenu') and contains(@text, 'AI Coder')]"))
                    aiCoderMenu.click()
                    log.info("'AI Coder' menu clicked")
                    true
                } catch (e: Exception) {
                    log.warn("Failed to find or click 'AI Coder' menu: ${e.message}")
                    false
                }
            }
            sleep(2000)
        }

        step("Click 'Generate Documentation' action") {
        TestUtil.speak("Initiating the 'Generate Documentation' action.")
            waitFor(Duration.ofSeconds(10)) {
                try {
                    findAll(CommonContainerFixture::class.java, byXpath("//div[contains(@class, 'ActionMenuItem') and contains(@text, 'Generate Documentation')]"))
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
        TestUtil.speak("Configuring documentation generation settings.")
            waitFor(Duration.ofSeconds(10)) {
                val dialog = find(CommonContainerFixture::class.java, byXpath("//div[@class='MyDialog' and @title='Compile Documentation']"))
                dialog.isShowing
            }
            val dialog = find(CommonContainerFixture::class.java, byXpath("//div[@class='MyDialog' and @title='Compile Documentation']"))
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
                TestUtil.speak("Documentation generation initiated with custom instructions.")
                true
            } catch (e: Exception) {
                log.warn("Failed to configure documentation generation: ${e.message}")
                false
            }
        }
        TestUtil.speak("AI is processing the request. This process typically takes a few seconds, depending on package size and complexity.")
        sleep(5000)

        step("Verify documentation creation")
        {
        TestUtil.speak("Verifying documentation creation and editor display.")
            waitFor(Duration.ofSeconds(60)) {
                try {
                    val editor = find(CommonContainerFixture::class.java, byXpath("//div[@class='EditorComponentImpl']"))
                    if (editor.isShowing) {
                        TestUtil.speak("Documentation successfully generated and displayed in the editor.")
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
            TestUtil.speak("Demo complete. The Generate Documentation feature has created comprehensive API documentation for the files utility package, demonstrating its efficiency in automating documentation tasks.")
            sleep(10000)
        }
    }
}