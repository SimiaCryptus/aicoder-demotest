package com.simiacryptus.aicoder.demotest.action.plan

import com.intellij.remoterobot.fixtures.CommonContainerFixture
import com.intellij.remoterobot.fixtures.JCheckboxFixture
import com.intellij.remoterobot.fixtures.JTreeFixture
import com.intellij.remoterobot.search.locators.byXpath
import com.intellij.remoterobot.stepsProcessing.step
import com.intellij.remoterobot.utils.waitFor
import com.simiacryptus.aicoder.demotest.DemoTestBase
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.openqa.selenium.By
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import org.slf4j.LoggerFactory
import java.lang.Thread.sleep
import java.time.Duration

/**
 * Test class for the Auto Plan Chat feature of AI Coder plugin.
 *
 * Prerequisites:
 * - IntelliJ IDEA must be running with the AI Coder plugin installed
 * - A project must be open with a standard structure
 * - The IDE should be in its default layout with no dialogs open
 *
 * Test Flow:
 * 1. Opens the Project View if not already visible
 * 2. Navigates to the source directory in the project tree
 * 3. Opens the AI Coder context menu
 * 4. Launches the Auto-Plan feature
 * 5. Interacts with the Auto-Plan web interface to:
 *    - Submit a task description
 *    - Monitor execution progress
 *    - View results
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AutoPlanActionTest : DemoTestBase() {

  companion object {
    private val log = LoggerFactory.getLogger(AutoPlanActionTest::class.java)
    private const val AUTO_PLAN_XPATH = "//div[contains(@class, 'ActionMenuItem') and contains(@text, 'Auto-Plan')]"
  }

  @Test
  fun testAutoPlanAction() = with(remoteRobot) {
    log.debug("Initializing Auto-Plan test execution")
    speak("Welcome to the AI Coder Auto-Plan demonstration. This feature provides automated planning and execution of coding tasks.")
    log.info("Starting Auto-Plan demonstration test")
    sleep(3000)

    step("Open project view") {
      log.debug("Attempting to open project view")
      speak("Opening the project view to access the file structure.")
      openProjectView()
      log.info("Project view opened successfully")
      sleep(2000)
    }

    step("Select source directory") {
      log.debug("Starting source directory selection")
      speak("Navigating to the source directory.")
      val path = arrayOf("src", "main", "kotlin")
      log.debug("Attempting to locate project tree with path: ${path.joinToString("/")}")
      val tree = find(JTreeFixture::class.java, byXpath(PROJECT_TREE_XPATH)).apply { expandAll(path) }
      log.debug("Project tree found, expanding path")
      waitFor(Duration.ofSeconds(10)) { tree.rightClickPath(*path, fullMatch = false); true }
      log.info("Source directory selected and right-clicked successfully")
      sleep(3000)
    }

    lateinit var aiCoderMenu: CommonContainerFixture
    step("Open AI Coder menu") {
      log.debug("Attempting to open AI Coder menu")
      speak("Opening the AI Coder menu.")
      aiCoderMenu = selectAICoderMenu()
      log.info("AI Coder menu opened successfully")
      speak("AI Coder menu opened.")
      sleep(3000)
    }

    step("Select Auto-Plan action") {
      log.debug("Starting Auto-Plan action selection")
      speak("Selecting the Auto-Plan feature.")
      waitFor(Duration.ofSeconds(10)) {
        try {
          val taskMenu = find(CommonContainerFixture::class.java, byXpath("//div[@text='AI Coder']//div[@text='\uD83D\uDCCB Task Plans']"))
            .apply {
              robot.mouseMove(this.locationOnScreen.x, aiCoderMenu.locationOnScreen.y)
              this.moveMouse()
            }
          sleep(1000)
          findAll(CommonContainerFixture::class.java, byXpath("//div[@text='AI Coder']//div[@text='\uD83E\uDD16 Auto-Plan']"))
            .firstOrNull()?.apply {
              robot.mouseMove(this.locationOnScreen.x, taskMenu.locationOnScreen.y)
              this.moveMouse()
              click()
            }
          log.info("Auto-Plan action found and clicked successfully")
          speak("Auto-Plan feature initiated.")
          true
        } catch (e: Exception) {
          log.warn("Failed to find Auto-Plan action. Error: ${e.message}", e)
          speak("Failed to find Auto-Plan action. Retrying...")
          false
        }
      }
      sleep(3000)
    }

    step("Configure Task Runner") {
      log.debug("Starting Task Runner configuration")
      speak("Configuring Task Runner settings.")
      waitFor(Duration.ofSeconds(10)) {
        val dialog = find(CommonContainerFixture::class.java, byXpath("//div[@class='MyDialog']"))
        if (dialog.isShowing) {
          log.debug("Configuration dialog found and visible")
          dialog.find(JCheckboxFixture::class.java, byXpath("//div[@text='Auto-apply fixes']")).apply {
            if (!isSelected()) {
              click()
              log.info("Auto-apply fixes checkbox toggled to selected state")
              speak("Enabled 'auto-apply fixes'; automatically apply suggested code changes.")
            }
          }
          dialog.find(JCheckboxFixture::class.java, byXpath("//div[@text='Allow blocking']")).apply {
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

    step("Interact with Auto-Plan interface") {
      log.debug("Starting Auto-Plan interface interaction")
      var url: String? = null
      waitFor(Duration.ofSeconds(90)) {
        val messages = getReceivedMessages()
        url = messages.firstOrNull { it.startsWith("http") } ?: ""
        log.debug("Searching for URL in messages. Found: ${url?.take(50) ?: "none"}")
        url?.isNotEmpty() ?: false
      }

      if (url != null) {
        log.info("Retrieved Auto-Plan interface URL: $url")
        speak("Auto-Plan web interface opened.")
        log.debug("Initializing web driver")
        driver.get(url)
        log.debug("Setting up WebDriverWait with 90 second timeout")

        val wait = WebDriverWait(driver, Duration.ofSeconds(90))
        val chatInput = wait.until(ExpectedConditions.elementToBeClickable(By.id("message-input")))
        log.info("Chat interface loaded successfully")
        speak("Interface loaded. Submitting task description.")
        sleep(1000)

        try {
          // Submit task description
          log.debug("Submitting task description to chat interface")
          chatInput.sendKeys("Create a new utility class for string manipulation with methods for common operations")
          speak("Submitting task: Create a new utility class for string manipulation.")

          clickElement(driver, wait, "#send-message-button")
          log.info("Task description submitted successfully")
          speak("Task description submitted.")
          sleep(1000)

          // Monitor execution progress
          log.debug("Beginning execution progress monitoring")
          speak("Auto-Plan is analyzing the task and creating an execution plan.")

          var planIndex = 3
          var hasStopped = false
          while (true) {

            if(planIndex > 8 && !hasStopped) {
              log.info("Reached maximum demonstration iterations, initiating stop sequence")
              speak("Auto-Plan can continue to iterate over agent plans. However, for demonstration purposes, we will stop here.")
              clickElement(driver, wait, ".tabs-container > div > button:nth-child(1)")
              log.debug("Controls button clicked")
              sleep(1000)
              clickElement(driver, wait, ".tabs-container > .tab-content.active > .href-link")
              log.info("Stop sequence completed")
              hasStopped = true
              sleep(1000)
            }

            val planButton = clickElement(driver, wait, "div.tabs-container > div.tabs > button:nth-child($planIndex)")
            if (planButton.text === "Summary") {
              log.info("Reached summary stage - execution plan complete")
              speak("Task analysis completed. Review the summary for the execution plan.")
              break
            } else {
              log.debug("Processing agent iteration ${planIndex - 2}: ${planButton.text}")
              speak("Agent iteration ${planIndex - 2} in progress.")
            }

            var taskIndex = 2
            while (true) {
              log.debug("Processing task iteration $taskIndex")
              val taskButton = clickElement(driver, wait, "div.tabs-container > div.active div.iteration.tabs-container > div.tabs > button:nth-child($taskIndex)")
              if (taskButton.text.trim().equals("Thinking Status", ignoreCase = true)) {
                log.info("Task ${taskIndex-1} execution completed")
                speak("Task execution complete. Updating Thinking State.")
                break;
              } else {
                log.debug("Executing task ${taskIndex-1}: ${taskButton.text}")
                speak("Task ${taskIndex-1} in progress.")
                sleep(3000)
              }
              taskIndex += 1
            }
            planIndex += 1
          }

          sleep(5000)
          log.info("Auto-Plan execution completed successfully")

          speak("Auto-Plan has completed the task execution. You can review the results in the interface.")
        } catch (e: Exception) {
          log.error("Error during Auto-Plan interaction: ${e.message}", e)
          speak("Encountered an error during Auto-Plan execution. Please check the logs for details.")
        } finally {
          log.debug("Cleaning up web driver resources")
          driver.quit()
          log.info("Web driver cleanup completed")
        }
      } else {
        log.error("Failed to retrieve Auto-Plan interface URL from UDP messages")
        speak("Failed to retrieve Auto-Plan interface URL.")
      }
      log.debug("Clearing message buffer")
      clearMessageBuffer()
    }
    log.info("Auto-Plan demonstration test completed successfully")
    speak("Auto-Plan demonstration completed. This feature automates the planning and execution of coding tasks, improving development efficiency.")
  }

}
