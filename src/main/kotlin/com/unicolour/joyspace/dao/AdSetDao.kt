package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.AdSet
import org.springframework.data.repository.PagingAndSortingRepository

interface AdSetDao : PagingAndSortingRepository<AdSet, Int>, AdSetCustomQuery
