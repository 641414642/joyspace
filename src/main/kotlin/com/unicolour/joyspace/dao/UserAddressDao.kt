package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.Address
import org.springframework.data.repository.CrudRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param


interface UserAddressDao : CrudRepository<Address, Int> {
    fun findByUserIdAndDeleted(userId: Int, deleted: Boolean): List<Address>

    @Modifying
    @Query("update Address a set a.default = false where a.userId = :userId and a.id != :id")
    fun updateDefault(@Param(value = "userId") userId: Int, @Param(value = "id") id: Int)

    @Modifying
    @Query("update Address a set a.deleted = true where a.id = :id")
    fun deleteS(@Param(value = "id") id: Int)
}