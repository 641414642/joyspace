package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.Position
import org.springframework.data.repository.PagingAndSortingRepository

interface PositionDao : PagingAndSortingRepository<Position, Int>
