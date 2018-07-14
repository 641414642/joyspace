package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.AdSet
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface AdSetCustomQuery {
    fun queryAdSets(pageable: Pageable, companyId: Int?, name: String, includePublicResource: Boolean): Page<AdSet>
    fun queryAdSets(companyId: Int?, name: String, includePublicResource: Boolean): List<AdSet>
}
