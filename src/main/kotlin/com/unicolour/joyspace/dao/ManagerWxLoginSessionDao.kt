package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.ManagerWxLoginSession
import org.springframework.data.repository.CrudRepository

interface ManagerWxLoginSessionDao : CrudRepository<ManagerWxLoginSession, String> {
    fun findByManagerId(managerId: Int) : ManagerWxLoginSession?
}
