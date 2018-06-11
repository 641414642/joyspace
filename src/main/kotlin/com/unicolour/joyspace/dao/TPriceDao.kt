package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.TPrice
import org.springframework.data.repository.CrudRepository
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.query.Param

interface TPriceDao : PagingAndSortingRepository<TPrice, Int> {

    fun findByPositionIdAndProductIdAndBeginLessThanAndExpireGreaterThanAndEnabled(positionId: Int, productId: Int, begin: Date, expire: Date, enabled: Boolean): List<TPrice>

    @Query("SELECT t FROM TPrice t WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    fun findByName(@Param("name") name: String, pageable: Pageable): Page<TPrice>


    @Query("SELECT t FROM TPrice t WHERE t.companyId=:companyId)")
    fun findByCompanyId(@Param("companyId") companyId: Int ,pageable: Pageable): Page<TPrice>



    @Query("SELECT t FROM TPrice t WHERE t.companyId=:companyId and LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    fun findByNameAndCompanyId(@Param("name") name: String, @Param("companyId") companyId: Int ,pageable: Pageable): Page<TPrice>



}
