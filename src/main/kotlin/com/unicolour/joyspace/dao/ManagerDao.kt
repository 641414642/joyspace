package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.Manager
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.query.Param

interface ManagerDao : PagingAndSortingRepository<Manager, Int> {
    @Query("SELECT u FROM Manager u WHERE u.companyId=:companyId AND (LOWER(u.userName) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :name, '%')))")
    fun findByCompanyIdAndUserNameOrFullName(@Param("companyId") companyId: Int, @Param("name") name: String, pageRequest: Pageable): Page<Manager>

    fun findByCompanyId(companyId: Int, pageRequest: Pageable): Page<Manager>
    fun findByUserNameOrFullName(userName: String, fullName: String): Manager?
    fun findByUserName(userName: String): Manager?
    fun findByWxOpenId(wxOpenId: String): Manager?
}
