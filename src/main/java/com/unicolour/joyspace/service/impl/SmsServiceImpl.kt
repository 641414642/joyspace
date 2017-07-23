package com.unicolour.joyspace.service.impl

import com.unicolour.joyspace.service.SmsService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

@Component
open class SmsServiceImpl : SmsService {
    @Value("\${com.unicolour.joyspace.smsServiceUrl}")
    lateinit var smsServiceUrl: String

    override fun send(phoneNumber: String, content: String): Pair<Int, String?> {
        val url = URL(String.format(smsServiceUrl,
                URLEncoder.encode(phoneNumber, "UTF-8"),
                URLEncoder.encode(content, "UTF-8")))

        val conn = url.openConnection() as HttpURLConnection

        conn.inputStream.use {
            val buf = StringBuilder()
            var r:Int
            do {
                r = it.read()
                if (r != -1) {
                    buf.append(r.toChar())
                }
            } while (r != -1)

            val t = buf.indexOf(',')
            if (t == -1) {
                return Pair(buf.toString().toInt(10), null)
            }
            else {
                return Pair(buf.substring(0, t).toInt(10), buf.substring(t+1))
            }
        }
    }
}

