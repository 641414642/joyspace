package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.TPrice
import org.springframework.data.repository.CrudRepository
import java.util.*

interface TPriceDao : CrudRepository<TPrice, Int> {
    fun findByCompanyIdAndProductIdAndBeginLessThanAndExpireGreaterThanAndEnabled(companyId: Int, productId: Int, begin: Date, expire: Date, enabled: Boolean): List<TPrice>
}
