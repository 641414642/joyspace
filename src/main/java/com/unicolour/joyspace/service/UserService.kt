package com.unicolour.joyspace.service

import com.unicolour.joyspace.dto.UserDTO
import com.unicolour.joyspace.model.User

interface UserService {
    fun createOrUpdateUser(user: UserDTO): User
}