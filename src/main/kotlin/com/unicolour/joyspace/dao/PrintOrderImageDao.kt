package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.PrintOrderImage
import com.unicolour.joyspace.model.PrintOrderProductImage
import org.springframework.data.repository.CrudRepository

interface PrintOrderImageDao : CrudRepository<PrintOrderImage, Int> {
    fun findByOrderItemIdAndName(orderItemId: Int, name: String) : PrintOrderImage?
    fun countByOrderIdAndUserImageFileIdIsNull(orderItemId: Int) : Long
    fun countByOrderIdAndUserImageFileIdIsNotNull(orderItemId: Int) : Long
}

interface PrintOrderProductImageDao : CrudRepository<PrintOrderProductImage, Int>{
    fun findByProductIdAndOrderId(productId: Int, orderId: Int): PrintOrderProductImage?

    fun findFirstByProductIdAndOrderIdOrderByIdDesc(productId: Int,orderId: Int):PrintOrderProductImage?
}