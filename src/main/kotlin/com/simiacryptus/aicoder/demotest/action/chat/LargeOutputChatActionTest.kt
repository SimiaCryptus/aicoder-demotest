package com.simiacryptus.aicoder.demotest.action.chat

import com.intellij.remoterobot.fixtures.CommonContainerFixture
import com.intellij.remoterobot.search.locators.byXpath
import com.intellij.remoterobot.stepsProcessing.step
import com.intellij.remoterobot.utils.waitFor
import com.simiacryptus.aicoder.demotest.DemoTestBase
import com.simiacryptus.aicoder.demotest.SplashScreenConfig
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.openqa.selenium.By
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import org.slf4j.LoggerFactory
import java.lang.Thread.sleep
import java.time.Duration

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LargeOutputChatActionTest : DemoTestBase(
  splashScreenConfig = SplashScreenConfig(
    titleText = "Large Output Chat Demo",
  )
) {
  companion object {
    val log = LoggerFactory.getLogger(LargeOutputChatActionTest::class.java)
  }

  @Test
  fun testLargeOutputChatAction() = with(remoteRobot) {
      tts("Welcome to the Large Output Chat demonstration. This feature provides an enhanced AI coding assistant with structured responses.")?.play(
          2000
      )

      step("Open Tools menu") {
          tts("Let's start by accessing the Large Output Chat through the Tools menu.")?.play()
          waitFor(Duration.ofSeconds(10)) {
              try {
                  log.debug("Attempting to find and click main menu")
                  find(CommonContainerFixture::class.java, byXpath("//div[@tooltiptext='Main Menu']")).click()
                  sleep(500)
                  log.info("Tools menu opened successfully")
                  true
              } catch (e: Exception) {
                  log.warn("Failed to open Tools menu: ${e.message}")
                  false
              }
          }
      }

      step("Click 'Large Output Chat' action") {
          tts("Now we'll navigate through the AI Coder menu to launch our Large Output Chat.")?.play()
          waitFor(Duration.ofSeconds(90)) {
              try {
                  log.debug("Attempting to find and click main menu")
                  find(CommonContainerFixture::class.java, byXpath("//div[@tooltiptext='Main Menu']")).click()
                  sleep(200)
                  find(CommonContainerFixture::class.java, byXpath("//div[@text='Help']")).moveMouse()

                  val toolsMenu = find(CommonContainerFixture::class.java, byXpath("//div[@text='Tools']"))
                  toolsMenu.moveMouse()


                  val chatMenu = toolsMenu.find(
                      CommonContainerFixture::class.java,
                      byXpath("//div[contains(@class, 'MenuItem') and contains(@text, 'Enhanced AI Chat')]")
                  )
                  robot.mouseMove(chatMenu.locationOnScreen.x + 10, toolsMenu.locationOnScreen.y)
                  chatMenu.click()

                  log.info("'Large Output Chat' action clicked")
                  true
              } catch (e: Exception) {
                  log.warn("Failed to find 'Large Output Chat' action: ${e.message}")
                  false
              }
          }
          sleep(2000)
      }

      step("Interact with chat interface") {
          var url: String? = null
          log.debug("Starting chat interface interaction")
          waitFor(Duration.ofSeconds(90)) {
              val messages = getReceivedMessages()
              url = messages.firstOrNull { it.startsWith("http") } ?: ""
              log.debug("Searching for URL in messages. Found: ${url?.take(50) ?: "none"}")
              url?.isNotEmpty() ?: false
          }

          if (url != null) {
              log.info("Retrieved chat interface URL: $url")
              tts("The chat interface has launched successfully.")?.play()
              driver.get(url)
              val wait = WebDriverWait(driver, Duration.ofSeconds(90))

              try {
                  val chatInput = wait.until(ExpectedConditions.elementToBeClickable(By.id("chat-input")))
                  log.info("Chat interface loaded successfully")
                  tts("Let's ask the AI about advanced Kotlin features.")?.play(1000)

                  log.debug("Submitting question to chat interface")
                  chatInput.click()
                  tts("Watch as we type our question.")?.play(1000)
                  chatInput.sendKeys("Can you explain advanced Kotlin features?")

                  wait.until(ExpectedConditions.elementToBeClickable(By.id("send-message-button"))).click()
                  log.info("Question submitted successfully")
                  tts("The question is submitted, and the AI is analyzing it.")?.play(2000)

                  // Wait for response
                  wait.until(ExpectedConditions.presenceOfElementLocated(By.className("response-message")))
                  tts("The AI has provided a detailed response.")?.play(3000)

                  log.info("Chat interaction completed successfully")
                  tts("We've successfully demonstrated the Large Output Chat feature.")?.play()
              } catch (e: Exception) {
                  log.error("Error during chat interaction: ${e.message}", e)
                  tts("Encountered an error during chat interaction. Please check the logs for details.")?.play()
              } finally {
                  log.debug("Cleaning up web driver resources")
                  driver.quit()
                  log.info("Web driver cleanup completed")
              }
          } else {
              log.error("Failed to retrieve chat interface URL from UDP messages")
              tts("Error: Unable to retrieve the necessary URL.")?.play()
          }
          log.debug("Clearing message buffer")
          clearMessageBuffer()
      }

      tts("This concludes our demonstration of the Large Output Chat. It provides structured responses for complex programming questions.")?.play(
          5000
      )
      return@with
  }
}