package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.VerifyCode
import org.springframework.data.repository.CrudRepository

interface VerifyCodeDao : CrudRepository<VerifyCode, String> {
}
