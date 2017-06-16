package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.PriceList
import org.springframework.data.repository.PagingAndSortingRepository

interface PriceListDao : PagingAndSortingRepository<PriceList, Int>
