package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.WxMpAccount
import org.springframework.data.repository.CrudRepository

interface WxMpAccountDao : CrudRepository<WxMpAccount, Int> {
    fun findFirstByActiveIsTrue(): WxMpAccount?
}
