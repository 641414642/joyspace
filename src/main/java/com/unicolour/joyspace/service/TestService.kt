package com.unicolour.joyspace.service

interface TestService {
    fun clearOldTestDataAndCreateNewTestData()
    fun isTestDatabase() : Boolean
}