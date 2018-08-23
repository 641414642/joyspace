package com.unicolour.joyspace

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.scheduling.annotation.EnableScheduling
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*

@SpringBootApplication
@EnableScheduling
open class JoySpaceApplication

fun main(args: Array<String>) {
    SpringApplication.run(JoySpaceApplication::class.java, *args)
    val fileName = UUID.randomUUID().toString()

    val srcImage = "/root/fileterImage/fileter${fileName}.jpeg"

    val desImage = "/root/fileterImage/fileter_${fileName}.jpeg"

    var filterImageJson = ProcessBuilder("python", "/root/joy_style/joy_api.py", srcImage, desImage);

    var process = filterImageJson.start();

    var retStr: String = "";
    var retError: String = ""
    BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
        retStr = reader.readText()
    }

    BufferedReader(InputStreamReader(process.errorStream)).use { reader ->
        retError = reader.readText()
    }
    println("aaaaaa=${retStr},bbbbb=${retError}")
}

