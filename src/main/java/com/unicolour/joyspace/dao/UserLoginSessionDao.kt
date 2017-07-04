package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.UserLoginSession
import org.springframework.data.repository.CrudRepository

interface UserLoginSessionDao : CrudRepository<UserLoginSession, String> {
    fun findByUserId(userId: Int) : UserLoginSession?
}
