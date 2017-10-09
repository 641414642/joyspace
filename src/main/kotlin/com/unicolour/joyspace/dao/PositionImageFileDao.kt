package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.PositionImageFile
import org.springframework.data.repository.CrudRepository

interface PositionImageFileDao : CrudRepository<PositionImageFile, Int> {
    fun findByPositionId(positionId: Int) : List<PositionImageFile>
}