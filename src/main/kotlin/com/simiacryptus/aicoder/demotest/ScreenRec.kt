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
import java.awt.Color
import java.awt.GraphicsEnvironment
import java.awt.Rectangle
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import javax.swing.JEditorPane
import javax.swing.JFrame
import javax.swing.SwingUtilities

open class ScreenRec {
  private var screenRecorder: ScreenRecorder? = null
  private val lock = Any()
  private val screenRecordingStarted = AtomicBoolean(false)
  private var splashFrame: JFrame? = null

  protected open fun showSplashScreen() {
    SwingUtilities.invokeLater {
      splashFrame = JFrame().apply {
        isUndecorated = true
        background = Color.WHITE
        extendedState = JFrame.MAXIMIZED_BOTH
        val editorPane = JEditorPane().apply {
          contentType = "text/html"
          text = splashPage()
          isEditable = false
          background = Color.WHITE
        }
        contentPane.add(editorPane)
        isVisible = true
      }
    }
  }

  protected open fun splashPage() = """
                            <html>
                            <body style='margin:0; padding:20px; background-color:white; display:flex; align-items:center; justify-content:center; height:100vh; text-align:center;'>
                                <div>
                                    <h1 style='font-size:48px; color:#333;'>Test Recording</h1>
                                    <h2 style='font-size:36px; color:#666;'>${testName}</h2>
                                    <p style='font-size:24px; color:#999;'>${SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())}</p>
                                </div>
                            </body>
                            </html>
                        """.trimIndent()

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
        showSplashScreen()
        Thread.sleep(1000) // Give splash screen time to appear

        val gd = GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice
        val captureSize = Rectangle(0, 0, 1920, 1080) // Adjust as needed
        val outputFolder = File("test-recordings")
        outputFolder.mkdirs()
        val fileFormat = Format(
          MediaTypeKey, MediaType.FILE,
          FormatKeys.MimeTypeKey, FormatKeys.MIME_AVI,
        )
        val screenFormat = Format(
          MediaTypeKey,
          MediaType.VIDEO,
          FormatKeys.EncodingKey,
          VideoFormatKeys.ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
          VideoFormatKeys.CompressorNameKey,
          VideoFormatKeys.ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
          VideoFormatKeys.DepthKey,
          24,
          FormatKeys.FrameRateKey,
          Rational(15, 1),
          VideoFormatKeys.QualityKey,
          1.0f,
          FormatKeys.KeyFrameIntervalKey,
          15 * 60
        )
        val mouseFormat = Format(
          MediaTypeKey, MediaType.VIDEO, FormatKeys.EncodingKey, "black", FormatKeys.FrameRateKey, Rational(15, 1)
        )
        val audioFormat = Format(
          MediaTypeKey, MediaType.AUDIO,
          //EncodingKey, audioFormatName,
          SampleRateKey, Rational.valueOf(44100.0), SampleSizeInBitsKey, 16, ChannelsKey, 2
        ) // Comment out or remove this block to disable audio recording
        try {
          log.info("Creating ScreenRecorder instance...")
          val recorder = ScreenRecorder(
            gd.defaultConfiguration, captureSize, fileFormat, screenFormat, mouseFormat, audioFormat,
            //null, // Pass null for audioFormat to disable audio recording
            outputFolder
          )
          log.info("Starting ScreenRecorder...")
          recorder.start()
          screenRecorder = recorder
          screenRecordingStarted.set(true)
          log.info("Screen recording started successfully")
          // Keep splash screen visible for 5 seconds after recording starts
          Thread {
            waitWithSplashDisplayed()
            hideSplashScreen()
          }.start()

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

  protected open fun waitWithSplashDisplayed() {
    Thread.sleep(5000)
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
        screenRecorder?.createdMovieFiles?.firstOrNull()?.absoluteFile?.apply {
          val dest = this.parentFile.resolve("$testName.${SimpleDateFormat("yyyyMMddHHmmss").format(Date())}.avi")
          waitFor("Waiting for file to exist: $this", timeoutMs = 10000) // Reduced timeout
          log.info("Rename $this to $dest")
          if (!this.renameTo(dest)) {
            log.error("Failed to rename $this to $dest")
          }
        } ?: log.warn("No screen recording file was created")
      } catch (e: Exception) {
        log.error("Error stopping screen recording", e)
      } finally {
        cleanupResources()
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