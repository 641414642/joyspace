package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.PrintOrderImage
import org.springframework.data.repository.CrudRepository

interface PrintOrderImageDao : CrudRepository<PrintOrderImage, Int> {
    fun findByOrderItemIdAndName(orderItemId: Int, name: String) : PrintOrderImage?
    fun countByOrderIdAndUserImageFileId(orderItemId: Int, userImageFileId: Int) : Long
}