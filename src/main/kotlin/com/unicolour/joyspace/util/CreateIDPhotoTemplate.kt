package com.unicolour.joyspace.util

import javafx.application.Application
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.stage.Stage
import org.apache.batik.apps.rasterizer.DestinationType
import org.apache.batik.apps.rasterizer.SVGConverter
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.nio.file.*
import java.util.*
import java.util.prefs.Preferences
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.imageio.ImageIO

fun main(args: Array<String>) {
    Application.launch(CreateIDPhotoTemplate::class.java)
}

class CreateIDPhotoTemplate : Application() {
    val pref = Preferences.userNodeForPackage(CreateIDPhotoTemplate::class.java)

    override fun start(primaryStage: Stage) {
        primaryStage.title = "证件照模板工具"
        val argTextArea = TextArea(
                pref.get("args",
"""USA-Passport 50 50 1x2 6x4 5 5
JPN-Passport 45 45 2x2 4x6 5 5
Z0604-02-004 35 53 2x2 4x6 5 5
Z0604-02-003 35 49 2x2 4x6 5 5
Z0604-02-002 35 45 2x2 4x6 5 5
Z0604-02-001 33 48 2x2 4x6 5 5
Z0604-01-003 26 32 2x4 6x4 5 5
Z0604-01-002 25 35 2x4 6x4 5 5
Z0604-01-001 22 32 2x4 6x4 5 5
Driving-License 21 26 2x4 6x4 5 5
Car 60 91 1x2 6x4 5 5"""))
        val dirTextField = TextField(pref.get("outputdir", "/Users/mojito/template"))
        val startButton = Button("生成")
        val box = VBox(10.0,
                Label("输出目录:"), dirTextField,
                Label("模板参数:(名称 小图宽度 小图高度 行数x列数 大图宽度x大图高度 小图水平间隙 小图垂直间隙)"), argTextArea,
                startButton)

        box.padding = Insets(10.0)
        VBox.setVgrow(argTextArea, Priority.ALWAYS)

        startButton.onAction = EventHandler{
            pref.put("args", argTextArea.text)
            pref.put("outputdir", dirTextField.text)

            box.children.forEach({ it.isDisable = true})
            Thread({
                startCreate(Paths.get(dirTextField.text), argTextArea.text.lines())
                Thread.sleep(1000)
                Platform.runLater({ box.children.forEach({ it.isDisable = false}) })
            }).start()
        }

        primaryStage.scene = Scene(box)
        primaryStage.show()
    }

    private fun startCreate(outputDir: Path, input: List<String>) {
        for (line in input) {
            if (line.isBlank()) {
                continue
            }

            val st = StringTokenizer(line, " ")
            val name = st.nextToken()

            val wStr = st.nextToken()
            val hStr = st.nextToken()

            val w = wStr.toDouble()  //照片宽度
            val h = hStr.toDouble()  //照片高度

            val rowCol = st.nextToken()
            val size = st.nextToken()

            val t1 = rowCol.indexOf('x')
            val t2 = size.indexOf('x')

            val row = rowCol.substring(0, t1).toInt() //照片行数
            val col = rowCol.substring(t1 + 1).toInt() //照片列数

            val tplW = size.substring(0, t2).toDouble() * 25.4   //相纸宽度
            val tplH = size.substring(t2 + 1).toDouble() * 25.4  //相纸高度

            val hGap = st.nextToken().toDouble()
            val vGap = st.nextToken().toDouble()

            val offsetX = (tplW - col * w - (col - 1) * hGap) / 2.0
            val offsetY = (tplH - row * h - (row - 1) * vGap) / 2.0

            var tpl =
"""<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<svg
   xmlns:dc="http://purl.org/dc/elements/1.1/"
   xmlns:cc="http://creativecommons.org/ns#"
   xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
   xmlns:svg="http://www.w3.org/2000/svg"
   xmlns="http://www.w3.org/2000/svg"
   xmlns:xlink="http://www.w3.org/1999/xlink"
   version="1.1"
   viewBox="0 0 ${tplW} ${tplH}"
   height="${tplH}mm"
   width="${tplW}mm">
"""
            var r = 0
            while (r < row) {
                var c = 0
                while (c < col) {
                    val x = offsetX + (w + hGap) * c
                    val y = offsetY + (h + vGap) * r

                    tpl +=
"""
  <image
     x="$x"
     y="$y"
     id="image_${r}_${c}"
     xlink:href="images/UserImagePlaceHolder.png"
     preserveAspectRatio="none"
     height="$h"
     width="$w">
    <desc>UserImage</desc>
    <title>照片</title>
  </image>
"""
                    c++
                }
                r++
            }

            val lineWidth = 0.1
            for (rIndex in 0 until row) {
                val y = offsetY + (h + vGap) * rIndex
                tpl += """<line x1="0" y1="${y - lineWidth/2}" x2="$tplW" y2="${y - lineWidth/2}" style="stroke:rgb(0,0,0);stroke-width:0.1mm" />"""
                tpl += """<line x1="0" y1="${y + h + lineWidth/2}" x2="$tplW" y2="${y + h + lineWidth/2}" style="stroke:rgb(0,0,0);stroke-width:0.1mm" />"""
            }
            for (cIndex in 0 until col) {
                val x = offsetX + (w + hGap) * cIndex
                tpl += """<line x1="${x - lineWidth/2}" y1="0" x2="${x - lineWidth/2}" y2="$tplH" style="stroke:rgb(0,0,0);stroke-width:0.1mm" />"""
                tpl += """<line x1="${x + w + lineWidth/2}" y1="0" x2="${x + w + lineWidth/2}" y2="$tplH" style="stroke:rgb(0,0,0);stroke-width:0.1mm" />"""
            }

            tpl += "</svg>"

            val tplDir = File(outputDir.toFile(), name)
            val tplFile = File(tplDir, "template.svg")
            val tplPreviewFile = File(tplDir, "template.html")
            val tplImagesDir = File(tplDir, "images")
            tplImagesDir.mkdirs()

            Files.write(tplFile.toPath(), tpl.toByteArray(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)

            val placeHolderImg = BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB)
            val g = placeHolderImg.createGraphics()
            g.color = Color.GRAY
            g.fillRect(0, 0, 10, 10)
            g.dispose()
            ImageIO.write(placeHolderImg, "png", File(tplImagesDir, "UserImagePlaceHolder.png"))

            //val maskImg = ImageIO.read(File("idPhotoMasks/${wStr}x${hStr}.png"))
            //ImageIO.write(maskImg, "png", File(tplDir, "mask.png"))
            Files.copy(Paths.get("idPhotoMasks/${wStr}x${hStr}.png"), File(tplDir, "mask.png").toPath(), StandardCopyOption.REPLACE_EXISTING)

            pack(tplDir.toPath(), outputDir.resolve("${name}.zip"))

            val previewHtml = """
                    <html>
                        <style>
                            svg {
                                border: 1px solid black;
                            }
                        </style>
                        <body>
                            $tpl
                        </body>
                    </html>
                """
            Files.write(tplPreviewFile.toPath(), previewHtml.toByteArray(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)

            //转 jpeg
            val svgConverter = SVGConverter()
            svgConverter.setSources(arrayOf(tplFile.absolutePath))
            svgConverter.destinationType = DestinationType.JPEG
            svgConverter.quality = 0.9f
            svgConverter.dst = tplDir
            svgConverter.backgroundColor = Color.WHITE
            svgConverter.execute()
        }
    }

    @Throws(IOException::class)
    fun pack(sourceDir: Path, zipFile: Path) {
        Files.deleteIfExists(zipFile)
        val p = Files.createFile(zipFile)
        ZipOutputStream(Files.newOutputStream(p)).use { zs ->
            Files.walk(sourceDir)
                    .filter { path -> !Files.isDirectory(path) }
                    .forEach { path ->
                        val zipEntry = ZipEntry(sourceDir.relativize(path).toString().replace('\\', '/'))
                        try {
                            zs.putNextEntry(zipEntry)
                            Files.copy(path, zs)
                            zs.closeEntry()
                        } catch (e: IOException) {
                            System.err.println(e)
                        }
                    }
        }
    }
}