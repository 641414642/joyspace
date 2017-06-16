package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.User
import org.springframework.data.repository.PagingAndSortingRepository

interface UserDao : PagingAndSortingRepository<User, Int> {
    fun findByWxOpenId(wxOpenId: String) : User?
}