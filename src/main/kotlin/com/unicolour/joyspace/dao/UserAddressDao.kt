package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.Address
import org.springframework.data.repository.CrudRepository

interface UserAddressDao : CrudRepository<Address, Int> {
    fun findByUserId(userId: Int): List<Address>
}