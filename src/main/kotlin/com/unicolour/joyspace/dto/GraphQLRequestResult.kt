package com.unicolour.joyspace.dto

class GraphQLRequestResult(var resultCode: ResultCode) {
    fun getResult() : Int {
        return resultCode.value
    }

    fun getDescription() : String {
        return resultCode.desc
    }
}

