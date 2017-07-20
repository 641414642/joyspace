package com.unicolour.joyspace.service

interface SmsService {

    fun send(phoneNumber: String, content: String): Boolean
}