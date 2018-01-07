package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.AdImageFile
import org.springframework.data.repository.PagingAndSortingRepository

interface AdImageFileDao : PagingAndSortingRepository<AdImageFile, Int>
