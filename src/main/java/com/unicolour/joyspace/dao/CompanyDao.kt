package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.Company
import org.springframework.data.repository.PagingAndSortingRepository

interface CompanyDao : PagingAndSortingRepository<Company, Int>
