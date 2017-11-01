package com.unicolour.joyspace.util

import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.StringTokenizer

object CreateIDPhotoTemplate {
    @JvmStatic
    fun main(args: Array<String>) {
        val input = arrayOf(
                "USA-Passport 50 50 2 1x2 6x4",
                "JPN-Passport 45 45 4 2x2 4x6",
                "Z0604-02-004 35 53 4 2x2 4x6",
                "Z0604-02-003 35 49 4 2x2 4x6",
                "Z0604-02-002 35 45 4 2x2 4x6",
                "Z0604-02-001 33 48 4 2x2 4x6",
                "Z0604-01-003 26 32 8 2x4 6x4",
                "Z0604-01-002 25 35 8 2x4 6x4",
                "Z0604-01-001 22 32 8 2x4 6x4")

        val gap = 2  //2mm 间隙

        for (line in input) {
            val st = StringTokenizer(line, " ")
            val name = st.nextToken()

            val w = st.nextToken().toDouble()  //照片宽度
            val h = st.nextToken().toDouble()  //照片高度
            val n = st.nextToken().toInt()  //照片个数

            val rowCol = st.nextToken()
            val size = st.nextToken()
            val t1 = rowCol.indexOf('x')
            val t2 = size.indexOf('x')

            val row = rowCol.substring(0, t1).toInt() //照片行数
            val col = rowCol.substring(t1 + 1).toInt() //照片列数

            val tplW = size.substring(0, t2).toDouble() * 25.4   //相纸宽度
            val tplH = size.substring(t2 + 1).toDouble() * 25.4  //相纸高度

            val offsetX = (tplW - col * w - (col - 1) * gap) / 2.0
            val offsetY = (tplH - row * h - (row - 1) * gap) / 2.0

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
                    val x = offsetX + (w + gap) * c
                    val y = offsetY + (h + gap) * r

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

            tpl += "</svg>"

            File("R:/tpl/${name}").mkdirs()
            Files.write(Paths.get("R:/tpl/${name}/template.svg"), tpl.toByteArray(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
        }
    }
}