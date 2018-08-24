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
}

