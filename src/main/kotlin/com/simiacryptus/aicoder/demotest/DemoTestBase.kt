package com.simiacryptus.aicoder.demotest

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.fixtures.CommonContainerFixture
import com.intellij.remoterobot.fixtures.JTreeFixture
import com.intellij.remoterobot.search.locators.byXpath
import com.intellij.remoterobot.utils.keyboard
import com.intellij.remoterobot.utils.waitFor
import com.simiacryptus.jopenai.OpenAIClient
import com.simiacryptus.jopenai.models.ApiModel
import com.simiacryptus.jopenai.models.AudioModels
import io.github.bonigarcia.wdm.WebDriverManager
import org.junit.jupiter.api.*
import org.openqa.selenium.*
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.lang.Thread.sleep
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentLinkedQueue
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import kotlin.concurrent.thread

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class DemoTestBase : ScreenRec() {
  protected lateinit var remoteRobot: RemoteRobot
  protected val robot: java.awt.Robot = java.awt.Robot()
  protected var testStartTime: LocalDateTime? = null
  protected lateinit var testProjectDir: Path
  private var isServerRunning = false
  private val messageBuffer = ConcurrentLinkedQueue<String>()

  protected var driverInitialized = false
  protected val driver: WebDriver by lazy { initializeWebDriver() }
  private fun initializeWebDriver(): ChromeDriver {
    try {
      log.info("Setting up ChromeDriver using WebDriverManager")
      WebDriverManager.chromedriver().setup()
      log.info("Configuring Chrome options")
      val options = ChromeOptions().apply {
        addArguments(
          "--start-maximized",
          "--remote-allow-origins=*",
          "--disable-dev-shm-usage",
          "--no-sandbox",
          "--disable-application-cache",
          "--kiosk",
        )
      }
      log.info("Initializing ChromeDriver with configured options")
      val driver = ChromeDriver(options)
      log.info("Setting browser zoom level to 150%")
      (driver as JavascriptExecutor).executeScript("document.body.style.zoom='150%'")
      driverInitialized = true
      log.info("ChromeDriver successfully initialized")
      return driver
    } catch (e: Exception) {
      log.error("Failed to initialize WebDriver: ${e.message}", e)
      throw RuntimeException("WebDriver initialization failed", e)
    }
  }

  @BeforeEach
  fun setUp(testInfo: TestInfo) {
    testStartTime = LocalDateTime.now()
    log.info("Starting test at ${testStartTime}")
  }

  @BeforeAll
  fun setup() {
    log.info("Starting test setup")
    remoteRobot = RemoteRobot("http://127.0.0.1:8082")
    log.info("RemoteRobot initialized with endpoint http://127.0.0.1:8082")
    startUdpServer()
    log.info("Setting Chrome driver system property")
    System.setProperty("webdriver.chrome.driver", "/usr/bin/chromedriver")
    try {
      log.info("Initializing test project")
      initializeTestProject()
      log.info("Starting screen recording")
      startScreenRecording()
    } catch (e: Exception) {
      log.error("Failed to start screen recording", e)
      throw e
    }
    log.info("Test setup completed successfully")
  }

  @AfterAll
  fun tearDown() {
    stopUdpServer()
    if (driverInitialized) {
      driver.quit()
    }
    stopScreenRecording()
    clearMessageBuffer()
    cleanupTestProject()
  }

  protected open fun getTemplateProjectPath(): String {
    return "demo_projects/TestProject"
  }

  private fun initializeTestProject() {
    log.info("Creating temporary directory for test project")
    // Create temporary directory
    testProjectDir = Files.createTempDirectory("test-project-")
    // Get template project path
    val templatePath = Paths.get(getTemplateProjectPath())
    log.debug("Template project path: $templatePath")
    if (!Files.exists(templatePath)) {
      log.error("Template project not found at path: $templatePath")
      throw IllegalStateException("Template project directory not found: $templatePath")
    }
    log.info("Copying template project to temporary directory")
    // Copy template project to temp directory
    templatePath.toFile().copyRecursively(testProjectDir.toFile(), true)
    log.info("Initialized test project in: $testProjectDir")

    val txt = testProjectDir.toString()
    log.debug("Opening project in IDE with path: $txt")
    waitFor(Duration.ofSeconds(20)) {
      try {
        log.debug("Attempting to find and click main menu")
        val menu = remoteRobot.find(CommonContainerFixture::class.java, byXpath("//div[@tooltiptext='Main Menu']"))
        menu.click()
        log.debug("Finding 'Open...' menu item")
        val open = remoteRobot.find(CommonContainerFixture::class.java, byXpath("//div[@text='File']//div[@text='Open...']"))
        robot.mouseMove(menu.locationOnScreen.x, open.locationOnScreen.y)
        open.click()
        sleep(3000)
        log.debug("Typing project path and pressing enter")
        remoteRobot.keyboard {
          this.enterText(txt.replace("\\", "\\\\"))
          this.enter()
          this.enter()
        }
        sleep(1000)
        remoteRobot.findAll(CommonContainerFixture::class.java, byXpath("//div[@text='Trust Project']")).firstOrNull()?.click()
        log.info("Project opened in IntelliJ IDEA")
        waitAfterProjectOpen()
        true
      } catch (e: Exception) {
        log.error("Failed to open project: ${e.message}", e)
        log.debug("Stack trace: ", e)
        false
      }
    }
  }

  protected open fun waitAfterProjectOpen() {
    sleep(15000)
  }

  private fun cleanupTestProject() {
    if (::testProjectDir.isInitialized) {
//      testProjectDir.toFile().deleteRecursively()
//      log.info("Cleaned up test project directory")
    }
  }

  protected fun JTreeFixture.expandAll(path: Array<String>) {
    (0 until path.size).forEach { i ->
      waitFor(Duration.ofSeconds(10)) {
        try {
          this.expand(*path.sliceArray(0..i))
          log.info("Navigated to ${path[i]}")
          true
        } catch (e: Exception) {
          false
        }
      }
    }
  }

  protected fun openProjectView() {
    waitFor(SHORT_TIMEOUT) {
      try {
        remoteRobot.find(CommonContainerFixture::class.java, byXpath(PROJECT_TREE_XPATH)).click()
        log.info("Project view opened")
        true
      } catch (e: Exception) {
        log.info("Failed to open project view: ${e.message}")
        false
      }
    }
  }

  protected fun selectAICoderMenu(): CommonContainerFixture {
    lateinit var aiCoderMenu: CommonContainerFixture
    log.debug("Attempting to find and select AI Coder menu")
    waitFor(SHORT_TIMEOUT) {
      try {
        aiCoderMenu = remoteRobot.find(CommonContainerFixture::class.java, byXpath(AI_CODER_MENU_XPATH)).apply {
          log.debug("Found AI Coder menu, waiting for visibility")
          // Ensure menu is visible before clicking
          waitFor(Duration.ofSeconds(2)) { isShowing }
          click()
        }
        log.info("'AI Coder' menu clicked")
        true
      } catch (e: Exception) {
        log.warn("Failed to find or click 'AI Coder' menu: ${e.message}", e)
        log.debug("Full exception details: ", e)
        false
      }
    }
    return aiCoderMenu
  }

  fun startUdpServer() {
    if (isServerRunning) return
    isServerRunning = true
    thread(isDaemon = true) {
      try {
        val socket = DatagramSocket(Companion.UDP_PORT)
        val buffer = ByteArray(1024)
        log.info("UDP server started on port ${Companion.UDP_PORT}")
        while (isServerRunning) {
          val packet = DatagramPacket(buffer, buffer.size)
          socket.receive(packet)
          val received = String(packet.data, 0, packet.length)
          log.info("Received UDP message: $received")
          messageBuffer.offer(received)
        }
        socket.close()
      } catch (e: Exception) {
        log.error("Error in UDP server", e)
      } finally {
        isServerRunning = false
      }
    }
  }

  fun stopUdpServer() {
    isServerRunning = false
  }

  fun getReceivedMessages(): List<String> {
    return messageBuffer.toList()
  }

  fun clearMessageBuffer() {
    messageBuffer.clear()
  }

  private val silent = true
  fun speak(text: String, voice: String = "shimmer") {
    if (silent) return
    log.info("Speaking: $text")
    val speechWavBytes = OpenAIClient().createSpeech(
      ApiModel.SpeechRequest(
        input = text, model = AudioModels.TTS.modelName, voice = voice, speed = 1.0, response_format = "wav"
      )
    ) ?: throw RuntimeException("No response")
    // Play the speech
    val byteInputStream = ByteArrayInputStream(speechWavBytes)
    AudioSystem.getAudioInputStream(byteInputStream).use { originalAudioInputStream ->
      val format = originalAudioInputStream.format
      val frameSize = format.frameSize
      val audioData = originalAudioInputStream.readAllBytes()
      // Ensure all values are positive
      for (i in audioData.indices) {
        audioData[i] = (audioData[i].toInt() and 0xFF).toByte()
      }
      val correctedAudioInputStream = AudioInputStream(ByteArrayInputStream(audioData), format, audioData.size.toLong() / frameSize)

      val clip: Clip = AudioSystem.getClip()
      clip.open(correctedAudioInputStream)
      clip.apply {
        start()
        // Wait for the audio to finish playing
        val millis = (frameLength * 1000L) / format.frameRate.toLong()
        log.info("Playing audio for $millis ms")
        sleep(millis)
        // Ensure the clip is closed after playing
        close()
      }
    }
    log.info("Audio playback completed")
  }

  companion object {
    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    const val PROJECT_TREE_XPATH: String = "//div[@class='ProjectViewTree']"
    const val AI_CODER_MENU_XPATH: String = "//div[contains(@class, 'ActionMenu') and contains(@text, 'AI Coder')]"
    val SHORT_TIMEOUT = Duration.ofSeconds(10)

    fun clickElement(driver: WebDriver, wait: WebDriverWait, selector: String) = runElement(
      driver, wait, selector, """
                arguments[0].scrollIntoView(true);
                arguments[0].click();
            """.trimIndent()
    )

    fun runElement(
      driver: WebDriver, wait: WebDriverWait, selector: String, js: String
    ): WebElement {
      val startTime = System.currentTimeMillis()
      while (true) {
        try {
          return wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(selector))).apply {
            (driver as JavascriptExecutor).executeScript(js, this)
          }
        } catch (e: WebDriverException) {
          if (e is TimeoutException) throw e
          if (System.currentTimeMillis() - startTime > 30000) throw e
          log.info("Failed to click $selector: ${e.message}")
          sleep(500)
        }
      }
    }

    fun <T> runElement(
      wait: WebDriverWait, selector: String, fn: (WebElement) -> T
    ): T {
      val startTime = System.currentTimeMillis()
      while (true) {
        try {
          return wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(selector))).let { fn(it) }
        } catch (e: WebDriverException) {
          if (e is TimeoutException) throw e
          if (System.currentTimeMillis() - startTime > 30000) throw e
          log.info("Failed to click $selector: ${e.message}")
          sleep(500)
        }
      }
    }

    val UDP_PORT = 41390
  }

}