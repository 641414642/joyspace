package com.unicolour.joyspace

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
open class JoySpaceApplication

fun main(args: Array<String>) {
    SpringApplication.run(JoySpaceApplication::class.java, *args)
}

