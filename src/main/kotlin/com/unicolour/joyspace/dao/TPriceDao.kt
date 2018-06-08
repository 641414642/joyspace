package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.TPrice
import org.springframework.data.repository.CrudRepository

interface TPriceDao : CrudRepository<TPrice, Int> {
    fun findByCompanyIdAndProductId(companyId: Int, productId: Int): TPrice?
}
