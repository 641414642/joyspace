package com.unicolour.joyspace.dto

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.User
import java.util.*

class LoginManagerDetail(val managerId: Int,
                         val companyId: Int,
                         val createTime: Calendar,
                         val fullName: String,
                         username: String,
                         password: String,
                         enabled: Boolean,
                         accountNonExpired: Boolean,
                         credentialsNonExpired: Boolean,
                         accountNonLocked: Boolean,
                         authorities: Collection<GrantedAuthority>) :
        User(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities) {
}
