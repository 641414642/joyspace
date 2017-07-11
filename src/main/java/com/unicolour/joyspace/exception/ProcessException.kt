package com.unicolour.joyspace.exception

class ProcessException(var errcode: Int, errMessage: String) : RuntimeException(errMessage)