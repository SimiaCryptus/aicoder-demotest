package com.github.simiacryptus.aicoder.demotest.single

import com.github.simiacryptus.aicoder.demotest.BaseActionTest
import com.intellij.remoterobot.fixtures.CommonContainerFixture
import com.intellij.remoterobot.fixtures.JCheckboxFixture
import com.intellij.remoterobot.fixtures.JTreeFixture
import com.intellij.remoterobot.search.locators.byXpath
import com.intellij.remoterobot.stepsProcessing.step
import com.intellij.remoterobot.utils.waitFor
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.openqa.selenium.*
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import org.slf4j.LoggerFactory
import java.lang.Thread.sleep
import java.time.Duration

/**
 * Test class for the Plan Ahead Action feature of AI Coder plugin.
 *
 * Prerequisites:
 * - IntelliJ IDEA must be running with the AI Coder plugin installed
 * - A project named "DataGnome" must be open
 * - The project must have a standard Kotlin project structure with src/main/kotlin directories
 * - The IDE should be in its default layout with no dialogs open
 *
 * Test Flow:
 * 1. Opens the Project View if not already visible
 * 2. Navigates to the kotlin source directory in the project tree
 * 3. Opens the AI Coder context menu
 * 4. Launches the Task Runner feature
 * 5. Configures the Task Runner with auto-fix enabled
 * 6. Interacts with the Task Runner web interface to:
 *    - Request adding a feature
 *    - Accept the AI's proposed changes
 *    - Wait for implementation
 *
 * Expected Results:
 * - The Task Runner should successfully complete the requested feature addition
 * - The test includes voice feedback for demonstration purposes
 * - All UI interactions should complete without errors
 *
 * Note: This test includes delays for demonstration purposes and voice narration
 * for educational/demo scenarios. These can be adjusted or removed for production testing.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PlanAheadActionTest : BaseActionTest() {

  companion object {
    private val log = LoggerFactory.getLogger(PlanAheadActionTest::class.java)
    private const val TASK_RUNNER_XPATH = "//div[contains(@class, 'ActionMenuItem') and contains(@text, 'Task Runner')]"
  }

  @Test
  fun testPlanAheadAction() = with(remoteRobot) {
    speak("Welcome to the AI Coder demo. We'll explore the Task Runner feature, which automates complex tasks to enhance coding workflow.")
    log.info("Starting testPlanAheadAction")
    sleep(3000)
    step("Open project view") {
      speak("Opening the project view to access the file structure.")
      openProjectView()
      sleep(2000)
    }

    step("Select source directory") {
      log.debug("Starting source directory selection")
      speak("Selecting a directory to initiate the Task Runner operation.")
      val path = arrayOf("src", "main", "kotlin")
      log.debug("Attempting to locate project tree with path: ${path.joinToString("/")}")
      val tree = remoteRobot.find(JTreeFixture::class.java, byXpath(PROJECT_TREE_XPATH)).apply { expandAll(path) }
      log.debug("Project tree found, expanding path")
      waitFor(Duration.ofSeconds(10)) { tree.rightClickPath(*path, fullMatch = false); true }
      log.info("Source directory selected and right-clicked successfully")
      sleep(3000)
    }

    step("Select 'AI Coder' menu") {
      log.debug("Attempting to open AI Coder menu")
      speak("Selecting the AI Coder option from the context menu.")
      selectAICoderMenu()
      log.info("AI Coder menu opened successfully")
      speak("AI Coder menu opened.")
      sleep(3000)
    }

    step("Click 'Task Runner' action") {
      log.debug("Starting Task Runner action selection")
      speak("Selecting the 'Task Runner' action.")
      waitFor(Duration.ofSeconds(10)) {
        try {
          findAll(CommonContainerFixture::class.java, byXpath(TASK_RUNNER_XPATH))
            .firstOrNull()?.click()
          log.info("Task Runner action found and clicked successfully")
          speak("Task Runner initiated.")
          true
        } catch (e: Exception) {
          log.warn("Failed to find 'Task Runner' action. Error: ${e.message}", e)
          speak("Failed to find Task Runner action. Retrying...")
          false
        }
      }
      sleep(3000)
    }

    step("Configure Task Runner") {
      log.debug("Starting Task Runner configuration")
      speak("Configuring Task Runner settings.")
      waitFor(Duration.ofSeconds(10)) {
        val dialog = find(CommonContainerFixture::class.java, byXpath("//div[@class='MyDialog' and @title='Configure Plan Ahead Action']"))
        if (dialog.isShowing) {
          log.debug("Configuration dialog found and visible")
          dialog.find(JCheckboxFixture::class.java, byXpath("//div[@class='JCheckBox' and @text='Auto-apply fixes']")).apply {
            if (!isSelected()) {
              click()
              log.info("Auto-apply fixes checkbox toggled to selected state")
              speak("Enabled 'auto-apply fixes'; automatically apply suggested code changes.")
            }
          }
          dialog.find(JCheckboxFixture::class.java, byXpath("//div[@class='JCheckBox' and @text='Allow blocking']")).apply {
            if (isSelected()) {
              click()
              log.info("Allow blocking checkbox toggled to deselected state")
              speak("Disabled 'blocking'; do not wait for user input.")
            }
          }
          sleep(3000)
          log.debug("Attempting to click OK button")

          val okButton = dialog.find(CommonContainerFixture::class.java, byXpath("//div[@class='JButton' and @text='OK']"))
          okButton.click()
          log.info("Task Runner configuration completed and dialog closed")
          speak("Task Runner configured and started.")
          true
        } else {
          log.warn("Configuration dialog not found or not visible")
          false
        }
      }
    }

    step("Interact with Task Runner interface") {
      log.debug("Starting Task Runner interface interaction")
      var url: String? = null
      waitFor(Duration.ofSeconds(90)) {
        val messages = getReceivedMessages()
        url = messages.firstOrNull { it.startsWith("http") } ?: ""
        log.debug("Searching for URL in messages. Found: ${url?.take(50) ?: "none"}")
        url?.isNotEmpty() ?: false
      }
      if (url != null) {
        log.info("Retrieved Task Runner interface URL: $url")
        speak("Task Runner web interface opened.")
        log.debug("Initializing web driver")
        initializeWebDriver()
        driver.get(url)
        log.debug("Setting up WebDriverWait with 90 second timeout")
        val wait = WebDriverWait(this@PlanAheadActionTest.driver, Duration.ofSeconds(90))
        val chatInput = wait.until(ExpectedConditions.elementToBeClickable(By.id("message-input")))
        log.info("Chat interface loaded successfully")
        speak("Interface loaded. Interacting with Task Runner.")
        sleep(1000)

        try {
          log.debug("Submitting task to chat interface")
          chatInput.sendKeys("Create a new data validation utility class with methods for common validation operations")
          clickElement(driver, wait, "#send-message-button")
          log.info("Task submitted successfully")
          speak("Submitting task: Create a new data validation utility class.")

          log.debug("Beginning execution progress monitoring")
          speak("Task Runner is analyzing the task and creating an execution plan.")

          var planIndex = 1
          while (true) {
            val duration = Duration.ofSeconds(if (planIndex == 1) 300 else 60)
            val wait = WebDriverWait(driver, duration)
            log.debug("Waiting $duration for plan iteration $planIndex")
            clickElement(driver, wait, "div.task-tabs.tabs-container > div.tabs > .tab-button:nth-child($planIndex)")
            val planButtonText = runElement(wait, "div.task-tabs.tabs-container > div.tabs > .tab-button:nth-child($planIndex)") { it.text}
            log.debug("Processing plan task $planIndex: ${planButtonText}")
            speak("Plan iteration $planIndex in progress: ${planButtonText}")
            sleep(500)
            runElement(
              driver, wait, "div.task-tabs.tabs-container > div.active div.tab-content", """
                arguments[0].scrollIntoView(true);
            """.trimIndent()
            )
            sleep(5000)
            planIndex += 1
          }
        } catch (e: Exception) {
          log.error("Error during Task Runner interaction: ${e.message}", e)
          speak("Encountered an error during Task Runner execution. Please check the logs for details.")
        } finally {
          log.debug("Cleaning up web driver resources")
          driver.quit()
          log.info("Web driver cleanup completed")
        }

      } else {
        log.error("Failed to retrieve Task Runner interface URL from UDP messages")
        speak("Failed to retrieve Task Runner interface URL.")
      }
      log.debug("Clearing message buffer")
      clearMessageBuffer()
    }
    log.info("Task Runner demonstration test completed successfully")

    speak("Task Runner demonstration completed. This feature automates the planning and execution of coding tasks, improving development efficiency.")
    sleep(10000)
  }

}