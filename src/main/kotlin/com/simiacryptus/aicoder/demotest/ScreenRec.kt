package com.simiacryptus.aicoder.demotest

import org.monte.media.av.Format
import org.monte.media.av.FormatKeys
import org.monte.media.av.FormatKeys.MediaType
import org.monte.media.av.FormatKeys.MediaTypeKey
import org.monte.media.av.codec.audio.AudioFormatKeys.*
import org.monte.media.av.codec.video.VideoFormatKeys
import org.monte.media.math.Rational
import org.monte.media.screenrecorder.ScreenRecorder
import org.slf4j.LoggerFactory
import java.awt.AWTException
import java.awt.GraphicsEnvironment
import java.awt.Rectangle
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import javax.swing.JFrame
import javax.swing.SwingUtilities

data class RecordingConfig(
  val outputFolder: File = File("test-recordings"),
  val captureSize: Rectangle = defaultResolution(),
  val frameRate: Rational = Rational(15, 1),
  val videoQuality: Float = 1.0f,
  val videoDepth: Int = 24,
  val keyFrameInterval: Int = 15 * 60,
  val sampleRate: Double = 44100.0,
  val sampleSize: Int = 16,
  val audioChannels: Int = 2,
  val enableAudio: Boolean = true,
  val splashScreenDuration: Long = 5000,
  val splashScreenDelay: Long = 1000,
  val fileFormat: String = FormatKeys.MIME_AVI,
  val videoEncoding: String = VideoFormatKeys.ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
  val mousePointerColor: String = "black",
  val outputFileNamePattern: String = "%s.%s.avi",
  val dateFormat: String = "yyyyMMddHHmmss",
  val waitForFileTimeout: Long = 10000,
  val waitForFileInterval: Long = 100
)

fun defaultResolution() = GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice.defaultConfiguration
  .bounds.let { bounds -> Rectangle(bounds.width, bounds.height) }

open class ScreenRec(
  protected val recordingConfig: RecordingConfig = RecordingConfig(),
  protected val splashScreenConfig: SplashScreenConfig = SplashScreenConfig()
) {
  // Add lifecycle methods to make the class more usable
  fun start() = startScreenRecording()
  fun stop() = stopScreenRecording()

  private var screenRecorder: ScreenRecorder? = null
  private val lock = Any()
  private val screenRecordingStarted = AtomicBoolean(false)
  private var splashFrame: JFrame? = null

  protected open fun hideSplashScreen() {
    SwingUtilities.invokeLater {
      splashFrame?.dispose()
      splashFrame = null
    }
  }

  protected open fun startScreenRecording() {
    synchronized(lock) {
      if (screenRecordingStarted.get()) {
        log.warn("Screen recording already started")
        return
      }
      try {
        log.info("Initializing screen recording...")
        SwingUtilities.invokeLater {
          this.splashFrame = splashScreenConfig.toSplashDialog(splashPage())
        }
        Thread.sleep(recordingConfig.splashScreenDelay)

        val gd = GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice
        val outputFolder = recordingConfig.outputFolder
        outputFolder.mkdirs()
        val fileFormat = Format(
          MediaTypeKey, MediaType.FILE,
          FormatKeys.MimeTypeKey, recordingConfig.fileFormat,
        )
        val screenFormat = Format(
          MediaTypeKey,
          MediaType.VIDEO,
          FormatKeys.EncodingKey, recordingConfig.videoEncoding,
          VideoFormatKeys.CompressorNameKey, recordingConfig.videoEncoding,
          VideoFormatKeys.DepthKey, recordingConfig.videoDepth,
          FormatKeys.FrameRateKey, recordingConfig.frameRate,
          VideoFormatKeys.QualityKey, recordingConfig.videoQuality,
          FormatKeys.KeyFrameIntervalKey, recordingConfig.keyFrameInterval
        )
        val mouseFormat = Format(
          MediaTypeKey, MediaType.VIDEO,
          FormatKeys.EncodingKey, recordingConfig.mousePointerColor,
          FormatKeys.FrameRateKey, recordingConfig.frameRate
        )
        val audioFormat = if (recordingConfig.enableAudio) Format(
          MediaTypeKey, MediaType.AUDIO,
          SampleRateKey, Rational.valueOf(recordingConfig.sampleRate),
          SampleSizeInBitsKey, recordingConfig.sampleSize,
          ChannelsKey, recordingConfig.audioChannels
        ) else null

        try {
          log.info("Creating ScreenRecorder instance...")
          val recorder = ScreenRecorder(
            gd.defaultConfiguration, recordingConfig.captureSize, fileFormat, screenFormat, mouseFormat, audioFormat, recordingConfig.outputFolder
          )
          log.info("Starting ScreenRecorder...")
          recorder.start()
          screenRecorder = recorder
          screenRecordingStarted.set(true)
          log.info("Screen recording started successfully")
          // Keep splash screen visible for 5 seconds after recording starts
          waitWithSplashDisplayed()
          hideSplashScreen()
        } catch (e: Exception) {
          log.error("Failed to initialize ScreenRecorder", e)
          log.error("GraphicsDevice: ${gd.iDstring}, isFullScreenSupported: ${gd.isFullScreenSupported}")
          log.error("OutputFolder exists: ${outputFolder.exists()}, canWrite: ${outputFolder.canWrite()}")
          hideSplashScreen()
          throw e
        }
      } catch (e: AWTException) {
        log.error("Failed to start screen recording", e)
        cleanupResources()
        hideSplashScreen()
        throw e
      } catch (e: IOException) {
        log.error("Failed to start screen recording", e)
        cleanupResources()
        hideSplashScreen()
        throw e
      } catch (e: Throwable) {
        log.error("Unexpected error during screen recording setup", e)
        cleanupResources()
        hideSplashScreen()
        throw e
      }
    }
  }

  protected open fun splashPage() = splashScreenConfig.splashPage()

  protected open fun waitWithSplashDisplayed() {
    Thread.sleep(recordingConfig.splashScreenDuration)
  }

  protected open val testName = this@ScreenRec.javaClass.simpleName

  protected open fun stopScreenRecording() {
    synchronized(lock) {
      if (!screenRecordingStarted.get()) {
        log.warn("Screen recording was not started, nothing to stop")
        return
      }
      try {
        log.info("Stopping screen recording...")
        screenRecorder?.stop()
        screenRecorder?.createdMovieFiles?.firstOrNull()?.absoluteFile?.let { recordedFile ->
          try {
            handleRecordedFile(recordedFile)
          } catch (e: Exception) {
            log.error("Error handling recorded file", e)
          }
        } ?: log.warn("No screen recording file was created")
      } catch (e: Exception) {
        log.error("Error stopping screen recording", e)
      } finally {
        cleanupResources()
      }
    }
  }

  private fun handleRecordedFile(recordedFile: File) {
    with(recordedFile) {
      val timestamp = SimpleDateFormat(recordingConfig.dateFormat).format(Date())
      val dest = this.parentFile.resolve(recordingConfig.outputFileNamePattern.format(testName, timestamp))
      waitFor(
        "Waiting for file to exist: $this",
        timeoutMs = recordingConfig.waitForFileTimeout,
        intervalMs = recordingConfig.waitForFileInterval
      )
      log.info("Rename $this to $dest")
      if (!this.renameTo(dest)) {
        log.error("Failed to rename $this to $dest")
      }
    }
  }

  private fun cleanupResources() {
    try {
      log.info("Cleaning up screen recording resources...")
      screenRecorder?.stop()
    } catch (e: Exception) {
      log.error("Error during resource cleanup", e)
    } finally {
      screenRecorder = null
      screenRecordingStarted.set(false)
      log.info("Screen recording resources cleaned up")
    }
  }

  companion object {
    private val log = LoggerFactory.getLogger(ScreenRec::class.java)
    fun File.waitFor(message: String, timeoutMs: Long = 10000, intervalMs: Long = 100) {
      require(timeoutMs > 0) { "Timeout must be positive" }
      require(intervalMs > 0) { "Interval must be positive" }

      val startTime = System.currentTimeMillis()
      while (!this.exists() || !this.canRead()) {
        if ((System.currentTimeMillis() - startTime) > timeoutMs) {
          log.warn("Timeout while $message")
          return
        }
        Thread.sleep(intervalMs)
      }

    }

  }

}