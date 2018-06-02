package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.Product
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.query.Param

interface ProductDao : PagingAndSortingRepository<Product, Int>, ProductCustomQuery {
    fun findByCompanyId(companyId: Int, pageable: Pageable): Page<Product>

    @Query("SELECT coalesce(max(p.sequence), 0) FROM Product p WHERE p.companyId=:companyId")
    fun getMaxProductSequence(@Param("companyId") companyId: Int): Int

    fun findFirstByCompanyIdAndSequenceGreaterThanOrderBySequence(companyId: Int, sequence: Int) : Product?

    fun findFirstByCompanyIdAndSequenceLessThanOrderBySequenceDesc(companyId: Int, sequence: Int) : Product?
    fun findByIdIn(idList: List<Int>): List<Product>

    fun findByTemplateIdInAndDeletedOrderBySequence(templateIdList: List<Int>, deleted: Boolean): List<Product>
}
