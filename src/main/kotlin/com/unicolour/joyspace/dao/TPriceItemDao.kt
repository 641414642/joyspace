package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.TPrice
import com.unicolour.joyspace.model.TPriceItem
import org.springframework.data.repository.CrudRepository

interface TPriceItemDao : CrudRepository<TPriceItem, Int> {

    fun deleteByTPriceId(TPrice: TPrice) : Int


    fun findByTPriceId(TPriceId: Int): List<TPriceItem>



//    fun companyDao.findAll(Sort(Sort.Order(Sort.Direction.ASC, "id")))
}
