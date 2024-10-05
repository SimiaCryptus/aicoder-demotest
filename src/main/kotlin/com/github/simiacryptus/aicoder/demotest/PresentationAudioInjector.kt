package com.github.simiacryptus.aicoder.demotest

import com.simiacryptus.jopenai.OpenAIClient
import com.simiacryptus.jopenai.models.ApiModel
import com.simiacryptus.jopenai.models.AudioModels
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.io.File
import java.io.FileOutputStream
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

object PresentationAudioInjector {

    @JvmStatic
    fun processPresentation(htmlFilePath: String, outputDir: String) {
        val htmlContent = File(htmlFilePath).readText()
        val document = Jsoup.parse(htmlContent)

        val sections = document.select("section")
        sections.forEachIndexed { index, section ->
            val notes = section.select("aside.notes").text()
            if (notes.isNotBlank()) {
                val mp3FilePath = "$outputDir/narration_$index.mp3"
                generateAudioFile(notes, mp3FilePath)
                injectAudioToSection(section, mp3FilePath)
            }
        }

        File(htmlFilePath).writeText(document.html())
    }

    private fun generateAudioFile(text: String, filePath: String) {
        val mp3Bytes = OpenAIClient().createSpeech(
            ApiModel.SpeechRequest(
                input = text,
                model = AudioModels.TTS.modelName,
                voice = "alloy",
                speed = 1.0,
                response_format = "mp3"
            )
        ) ?: throw RuntimeException("No response")
        FileOutputStream(filePath).use { it.write(mp3Bytes) }
    }

    private fun injectAudioToSection(section: Element, audioFilePath: String) {
        val audioElement = Element("audio")
        audioElement.attr("controls", "controls")
        audioElement.appendElement("source").attr("src", audioFilePath).attr("type", "audio/mpeg")
        section.prependChild(audioElement)
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val htmlFileChooser = JFileChooser().apply {
            dialogTitle = "Select HTML File"
            fileFilter = FileNameExtensionFilter("HTML files", "html", "htm")
            isAcceptAllFileFilterUsed = false
        }
        val outputDirChooser = JFileChooser().apply {
            dialogTitle = "Select Output Directory"
            fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
        }
        if (htmlFileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            val htmlFile = htmlFileChooser.selectedFile
            if (outputDirChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                val outputDir = outputDirChooser.selectedFile
                processPresentation(htmlFile.absolutePath, outputDir.absolutePath)
                println("Processing completed successfully.")
            } else {
                println("Output directory selection cancelled.")
            }
        } else {
            println("HTML file selection cancelled.")
        }
    }
}