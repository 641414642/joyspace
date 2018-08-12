package com.unicolour.joyspace.controller.api.v2

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

@RestController
class ApiImageRoute {
    @PostMapping("v2/image/convert")
    fun convert() {
        try {
            val srcImage = "/root/joyTest/test0805.jpeg"
            val desImage = "/root/joyTest/test0805_.jpeg"

            val process = ProcessBuilder("python", "/root/JoySpace-Filter/joy_filter.py",srcImage,desImage).start()

            var retStr = ""
            var retError = ""
            BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                retStr = reader.readText()
            }
            BufferedReader(InputStreamReader(process.errorStream)).use { reader ->
                retError = reader.readText()
            }

            val retCode = process.waitFor()
            println("retStr:$retStr,retCode:$retCode,retError:$retError")

        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }
}



