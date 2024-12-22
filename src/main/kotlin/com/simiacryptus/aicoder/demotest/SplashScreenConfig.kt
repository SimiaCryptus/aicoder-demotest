package com.simiacryptus.aicoder.demotest

import java.awt.GraphicsEnvironment
import java.awt.Rectangle
import java.text.SimpleDateFormat
import java.util.*
import javax.swing.JEditorPane
import javax.swing.JFrame

data class SplashScreenConfig(
  val fontFamily: String = "Roboto",
  val titleColor: String = "#2c3e50",
  val subtitleColor: String = "#34495e",
  val timestampColor: String = "#7f8c8d",
  val titleText: String = "Test Recording",
  //val dateFormat: String = "yyyy-MM-dd HH:mm:ss",
  // Long pretty format
  val dateFormat: String = "dddd, MMMM d, yyyy 'at' h:mm:ss a",
  val containerStyle: String = "background: white; padding: 20px; border-radius: 20px; box-shadow: 0 10px 30px rgba(0,0,0,0.1); animation: fadeIn 1s ease-in;",
  val bodyStyle: String = "margin: 0; padding: 0; display: flex; align-items: center; justify-content: center; height: 100vh; text-align: center;",
  val subtitle: String = "🔮🤖 Simiacryptus AI Coding Assistant 🤖🔮"
) {
  private val cssStyles = """
    @import url('https://fonts.googleapis.com/css2?family=${fontFamily}:wght@300;400;700&display=swap');
    body { font-family: '${fontFamily}', sans-serif; ${bodyStyle} }
    .container { ${containerStyle} }
    h1 { font-size: 48px; color: ${titleColor}; margin-bottom: 0; font-weight: 700; }
    h2 { font-size: 36px; color: ${subtitleColor}; margin-bottom: 0; font-weight: 400; }
    p { font-size: 24px; color: ${timestampColor}; font-weight: 300; }
  """.trimIndent()

  fun splashPage(): String {
    val screenSize = getScreenSize()
    return """
      <!DOCTYPE html>
      <html>
      <head><style>${cssStyles}</style></head>
      <body>
        <div style="height: ${screenSize.height / 3}px"></div>
        <div class="container" style="height: ${screenSize.height / 3}px">
          <h1>${titleText}</h1>
          <h2>$subtitle</h2>
          <p>${SimpleDateFormat(dateFormat).format(Date())}</p>
        </div>
      </body>
      </html>
    """.trimIndent()
  }

  fun getScreenSize() = GraphicsEnvironment.getLocalGraphicsEnvironment()
    .defaultScreenDevice.defaultConfiguration.bounds.run {
      Rectangle(width, height)
    }

}

fun SplashScreenConfig.toSplashDialog(page: String) = JFrame().apply {
  val screenSize = getScreenSize()
  isUndecorated = true
  isAlwaysOnTop = true
  extendedState = JFrame.MAXIMIZED_BOTH
  size = screenSize.size
  contentPane.add(JEditorPane().apply {
    contentType = "text/html"
    text = page
    isEditable = false
    size = screenSize.size
  })
  isVisible = true
}
