package com.unicolour.joyspace.controller.api

import com.unicolour.joyspace.service.PrintOrderService
import org.apache.commons.codec.binary.Hex
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.io.InputStreamReader
import java.io.UnsupportedEncodingException
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.servlet.http.HttpServletRequest

@RestController
class WeixinNotifyController {
    companion object {
        val logger = LoggerFactory.getLogger(WeixinNotifyController::class.java)
    }

//    @Value("\${com.unicolour.wxNotifyToken}")
//    lateinit var wxNotifyToken: String

    @Autowired
    lateinit var printOrderService: PrintOrderService

    @RequestMapping("/wxpay/notify", method = arrayOf(RequestMethod.POST), produces = arrayOf(MediaType.APPLICATION_XML_VALUE))
    fun wxPayNotify(request: HttpServletRequest) : ResponseEntity<String> {
        InputStreamReader(request.inputStream, StandardCharsets.UTF_8).use {
            reader ->
            val errmsg = printOrderService.processWxPayNotify(reader.readText())
            val retCode = if (errmsg == null) "SUCCESS" else "FAIL"
            val retMsg =  if (errmsg == null) "OK" else errmsg

            return ResponseEntity.ok("<xml>" +
                    "<return_code><![CDATA[$retCode]]></return_code>" +
                    "<return_msg><![CDATA[$retMsg]]></return_msg>" +
                    "</xml>")
        }
    }

//    @RequestMapping("/wxmp/notify", method = arrayOf(RequestMethod.GET))
//    @ResponseBody
//    fun weixinMpNotify(@RequestParam("signature", required = true) signature: String,
//                       @RequestParam("timestamp", required = true) timestamp: String,
//                       @RequestParam("nonce", required = true) nonce: String,
//                       @RequestParam("echostr", required = true) echostr: String
//    ) : String {
//
//        logger.info("/wxmp/notify, signature=$signature, timestamp=$timestamp, nonce=$nonce, echostr=$echostr")
//        return if (checkSignature(signature, timestamp, nonce)) {
//            logger.info("/wxmp/notify check pass")
//            echostr
//        }
//        else {
//            logger.info("/wxmp/notify check failed")
//            ""
//        }
//    }

//    private fun checkSignature(signature: String, timestamp: String, nonce: String): Boolean {
//        try {
//            val arr = arrayOf(timestamp, nonce, wxNotifyToken)
//            Arrays.sort(arr)
//
//            val s = arr[0] + arr[1] + arr[2]
//            val md = MessageDigest.getInstance("SHA-1")
//            val digest = md.digest(s.toByteArray(charset("utf-8")))
//
//            return signature.equals(Hex.encodeHexString(digest), ignoreCase = true)
//        } catch (e: NoSuchAlgorithmException) {
//            e.printStackTrace()
//            return false
//        } catch (e: UnsupportedEncodingException) {
//            e.printStackTrace()
//            return false
//        }
//    }
}
